package com.livebmw.metro.application.service;

import com.livebmw.metro.domain.entity.MetroLine;
import com.livebmw.metro.domain.repository.MetroLineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MetroLineService {

    private final MetroLineRepository metroLineRepository;

    private final Map<Integer, MetroLine> byCodeCache = new ConcurrentHashMap<>();
    private final Map<String, MetroLine> byNormalizedNameCache = new ConcurrentHashMap<>();

    public MetroLine ofCode(Integer code) {
        if (code == null) throw new IllegalArgumentException("code cannot be null");
        return byCodeCache.computeIfAbsent(code, c ->
                metroLineRepository.findById(c).orElseThrow(() -> new IllegalArgumentException(c + "를 찾을 수 없습니다."))
        );
    }

    public MetroLine ofName(String name) {
        if (name == null) return null;
        String key = normalize(name);

        // 표시명(정확 일치) 먼저: displayName 컬럼을 정규화 비교하려면 한 번 로드 필요
        // 규모가 크지 않다면 전체 미리 캐시해도 됨
        MetroLine exact = byNormalizedNameCache.get(key);
        if (exact != null) return exact;

        // 1) displayName 일치 탐색 (간단히 전체 로드 후 캐시 구축)
        if (byNormalizedNameCache.isEmpty()) {
            metroLineRepository.findAll().forEach(l ->
                    byNormalizedNameCache.putIfAbsent(normalize(l.getLineName()), l)
            );
        }
        MetroLine fromDisplay = byNormalizedNameCache.get(key);
        return fromDisplay;
    }

    public MetroLine findById(int id) {
        return ofCode(id);
    }

    public MetroLine findByLineName(String lineName) {
        if (lineName == null) return null;
        String s = lineName.trim();
        if (s.chars().allMatch(Character::isDigit)) {
            try {
                return ofCode(Integer.parseInt(s));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(lineName + " is not a valid code");
            }
        }
        return ofName(s);
    }

    public String toDisplayName(Integer code) {
        MetroLine line = ofCode(code);
        return line != null ? line.getLineName() : String.valueOf(code);
    }

    public boolean matches(Integer code, String displayName) {
        MetroLine line = ofCode(code);
        return line != null && line.getLineName().equals(displayName);
    }

    /** enum의 normalize 동작 이식 */
    public static String normalize(String s) {
        if (s == null) return null;
        // NFKC 정규화로 유사 공백/호환 문자 정리 후 공백 제거
        String n = Normalizer.normalize(s, Normalizer.Form.NFKC)
                .replace("\uFEFF","")
                .replaceAll("\\s+","")
                .trim();
        return n;
    }
}
