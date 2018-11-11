package com.mandan.server.main;

import java.util.Calendar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.mandan.services.DownloadPrices;

import ch.qos.logback.core.net.SyslogOutputStream;

@SpringBootApplication
public class SpringBootPriceQueryApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootPriceQueryApplication.class, args);		
		
	}
}
