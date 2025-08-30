package com.livebmw.metro.application.init;

import com.livebmw.common.util.GeometryFactory4326;
import com.livebmw.metro.config.MetroStationImportProps;
import com.livebmw.metro.domain.entity.MetroLine;
import com.livebmw.metro.domain.entity.MetroStation;
import com.livebmw.metro.domain.repository.MetroLineRepository;
import com.livebmw.metro.domain.repository.MetroStationRepository;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(MetroStationImportProps.class)
public class MetroStationCsvLoader implements ApplicationRunner {

    private final ResourceLoader resourceLoader;
    private final MetroLineRepository lineRepo;
    private final MetroStationRepository stationRepo;
    private final MetroStationImportProps props;

    private static final GeometryFactory WGS84_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);


    private static final List<String> K_STATION_ID = List.of("station_id","지하철역ID","STATN_ID","전철역코드");
    private static final List<String> K_LINE_ID    = List.of("line_id","지하철호선ID");
    private static final List<String> K_NAME       = List.of("station_name","지하철역명","STATN_NM");
    private static final List<String> K_ADDR       = List.of("address","기본주소");
    private static final List<String> K_LON        = List.of("lon","경도");
    private static final List<String> K_LAT        = List.of("lat","위도");
    private static final List<String> K_PREV       = List.of("prev_station_id","이전지하철역ID");
    private static final List<String> K_NEXT       = List.of("next_station_id","다음지하철역ID");

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!props.enabled()) {
            log.info("[MetroStationCsvLoader] disabled. skip.");
            return;
        }

        if (stationRepo.count() > 0) {
            log.info("[MetroStationCsvLoader] metro_station already has data. skip.");
            return;
        }

        String csvLocation = props.coordCsv();
        Resource res = resourceLoader.getResource(csvLocation);
        if (!res.exists()) throw new FileNotFoundException("CSV not found: " + csvLocation);

        // Line cache
        Map<Integer, MetroLine> lineCache = lineRepo.findAll().stream()
                .collect(Collectors.toMap(MetroLine::getLineId, l -> l));
        if (lineCache.isEmpty()) {
            log.warn("[MetroStationCsvLoader] metro_line is empty. insert metro_line first.");
        }

        int inserted = 0, bad = 0, unknownLine = 0;
        List<MetroStation> batch = new ArrayList<>(1000);

        try (Reader isr = new InputStreamReader(removeBOM(res.getInputStream()), StandardCharsets.UTF_8);
             CSVReader reader = new CSVReader(isr)) {

            String[] header = reader.readNext();
            if (header == null) throw new IllegalStateException("Empty CSV: " + csvLocation);
            Map<String,Integer> idx = indexOf(header);

            String[] r;
            int lineNo = 1;
            while ((r = reader.readNext()) != null) {
                lineNo++;

                Integer stationId = parseIntSafe(pick(r, idx, K_STATION_ID));
                Integer lineId    = parseIntSafe(pick(r, idx, K_LINE_ID));
                String  name      = limit(strip(pick(r, idx, K_NAME)), 60);
                String  address   = limit(emptyToNull(strip(pick(r, idx, K_ADDR))), 200);
                Double  lon       = parseDoubleSafe(pick(r, idx, K_LON));
                Double  lat       = parseDoubleSafe(pick(r, idx, K_LAT));
                Integer prevId    = parseIntSafe(pick(r, idx, K_PREV));
                Integer nextId    = parseIntSafe(pick(r, idx, K_NEXT));

                if (stationId == null || lineId == null || isBlank(name)) {
                    bad++;
                    if (bad <= 5) log.warn("Bad row {} -> required missing: {}", lineNo, Arrays.toString(r));
                    continue;
                }

                MetroLine line = lineCache.get(lineId);
                if (line == null) {
                    unknownLine++;
                    if (unknownLine <= 5) log.warn("Unknown line_id {} at row {} (stationId={})", lineId, lineNo, stationId);
                    continue;
                }

                if (lon == null || lat == null) {
                    lon = 0.0D;
                    lat = 0.0D;
                }
                Point geom = toPoint(lon, lat);


                MetroStation st = MetroStation.builder()
                        .stationId(stationId)
                        .line(line)
                        .stationName(name)
                        .address(address)
                        .geom(geom)
                        .prevStationId(prevId)
                        .nextStationId(nextId)
                        .build();

                batch.add(st);
                if (batch.size() >= 500) {
                    stationRepo.saveAll(batch);
                    inserted += batch.size();
                    batch.clear();
                    log.info("saved 500 rows... inserted={}", inserted);
                }
            }
        } catch (CsvValidationException e) {
            throw new RuntimeException("CSV parse error: " + e.getMessage(), e);
        }

        if (!batch.isEmpty()) {
            stationRepo.saveAll(batch);
            inserted += batch.size();
        }

        log.info("[MetroStationCsvLoader] DONE. inserted={}, bad={}, unknownLine={}", inserted, bad, unknownLine);
    }

    // ---------- helpers ----------
    private static Map<String,Integer> indexOf(String[] header) {
        Map<String,Integer> idx = new HashMap<>();
        for (int i=0;i<header.length;i++) idx.put(strip(header[i]), i);
        return idx;
    }
    private static String pick(String[] row, Map<String,Integer> idx, List<String> keys) {
        for (String k: keys) {
            Integer i = idx.get(k);
            if (i!=null && i>=0 && i<row.length) return row[i];
        }
        return null;
    }
    private static String strip(String s) {
        if (s==null) return null;
        return s.replace("\uFEFF","").trim();
    }
    private static String emptyToNull(String s) {
        return (s==null || s.isEmpty()) ? null : s;
    }
    private static boolean isBlank(String s) {
        return s==null || s.trim().isEmpty();
    }
    private static Integer parseIntSafe(String s) {
        try { return isBlank(s) ? null : Integer.valueOf(s.trim()); }
        catch (Exception e) { return null; }
    }
    private static Double parseDoubleSafe(String s) {
        try { return isBlank(s) ? null : Double.valueOf(s.trim()); }
        catch (Exception e) { return null; }
    }
    private static String limit(String s, int max) {
        if (s==null) return null;
        return s.length()<=max ? s : s.substring(0, max);
    }
    private static InputStream removeBOM(InputStream in) throws IOException {
        PushbackInputStream pb = new PushbackInputStream(in, 3);
        byte[] bom = new byte[3];
        int n = pb.read(bom, 0, 3);
        if (n == 3) {
            if (!(bom[0]==(byte)0xEF && bom[1]==(byte)0xBB && bom[2]==(byte)0xBF)) {
                pb.unread(bom, 0, n);
            }
        } else if (n > 0) {
            pb.unread(bom, 0, n);
        }
        return pb;
    }

    private static Point toPoint(double lon, double lat) {
        return WGS84_FACTORY.createPoint(new Coordinate(lon, lat));
    }
}
