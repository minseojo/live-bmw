package com.livebmw;

import com.livebmw.metrostation.application.job.MetroLineAndStationCsvImporter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class LiveBmwApplication {

	public static void main(String[] args) {
		SpringApplication.run(LiveBmwApplication.class, args);
	}

	@Component
	@RequiredArgsConstructor
	public static class BootstrapRunner implements CommandLineRunner {

		private final MetroLineAndStationCsvImporter importer;

		@Override
		public void run(String... args) throws Exception {
			importer.importFrom();
		}
	}
}
