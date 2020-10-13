/*
 * Copyright (c) 2017, Adam <Adam@sigterm.info>
 * Copyright (c) 2020, Null <TheRealNull@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package service;

import ch.qos.logback.classic.LoggerContext;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import net.runelite.cache.fs.Store;
import org.slf4j.ILoggerFactory;
import org.slf4j.impl.StaticLoggerBinder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import service.xtea.XteaController;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.logging.Logger;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableScheduling
@Slf4j
public class SpringBootWebApplication extends SpringBootServletInitializer
{
	public static Store store;

	@Bean
	protected ServletContextListener listener()
	{
		return new ServletContextListener()
		{
			@Override
			public void contextInitialized(ServletContextEvent sce)
			{
				log.info("OpenOSRS session manager started");
			}

			@Override
			public void contextDestroyed(ServletContextEvent sce)
			{
				log.info("OpenOSRS session manager stopped");
			}

		};
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application)
	{
		return application.sources(SpringBootWebApplication.class);
	}

	@Override
	public void onStartup(ServletContext servletContext) throws ServletException
	{
		super.onStartup(servletContext);
		ILoggerFactory loggerFactory = StaticLoggerBinder.getSingleton().getLoggerFactory();
		if (loggerFactory instanceof LoggerContext)
		{
			LoggerContext loggerContext = (LoggerContext) loggerFactory;
			loggerContext.setPackagingDataEnabled(false);
			log.debug("Disabling logback packaging data");
		}
	}

	public static void main(String[] args)
	{
		try {
			Logger.getAnonymousLogger().info("Initializing cache store for key verification");
			store = new Store(new File(args[0]));
		} catch (IOException e) {
			System.out.println("argo0: valid cache_dir not provided, keys cannot be verified. shutting down.");
			System.exit(1);
		}
		try {
			Type type = new TypeToken<HashMap<Integer, int[]>>(){}.getType();
			BufferedReader bufferedReader = new BufferedReader(new FileReader("./xteas.json"));
			Gson gson = new Gson();
			XteaController.xteas = gson.fromJson(bufferedReader, type);
		} catch (FileNotFoundException e) {
			log.warn("Xteas backup not found, starting new databse.");
			e.printStackTrace();
		}
		SpringApplication.run(SpringBootWebApplication.class, args);
	}

	@Component
	public class ServerPortCustomizer
			implements WebServerFactoryCustomizer<ConfigurableWebServerFactory> {

		@Override
		public void customize(ConfigurableWebServerFactory factory) {
			factory.setPort(8089);
		}
	}
}
