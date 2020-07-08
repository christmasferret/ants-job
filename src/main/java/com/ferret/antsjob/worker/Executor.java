package com.ferret.antsjob.worker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ferret.antsjob.common.CityWeather;
import com.ferret.antsjob.common.JobEvent;
import com.ferret.antsjob.common.JobExecuteInfo;
import com.ferret.antsjob.common.JobExecuteResult;
import com.ferret.antsjob.lock.JobLock;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Component
@Profile("worker")
public class Executor {

    @Autowired
    WkrJobManager wkrJobManager;

    Random rand = new Random();

    public Executor() {
    }

    public JobExecuteResult executeJob(JobExecuteInfo jobExecuteInfo) {
        JobExecuteResult jobExecuteResult = new JobExecuteResult();
        jobExecuteResult.setJobExecuteInfo(jobExecuteInfo);
        jobExecuteResult.setStartTime(ZonedDateTime.now());
        JobLock jobLock = null;

        try {
            Thread.sleep(rand.nextInt(500));    // add a random delay to distributed job to all nodes
            jobLock = wkrJobManager.createJobLock(jobExecuteInfo.getJob().getName());
            if (!jobLock.tryLock()) {
                jobExecuteResult.setEndTime(ZonedDateTime.now());
            } else {
                jobExecuteResult.setStartTime(ZonedDateTime.now());

                OkHttpClient client = new OkHttpClient().newBuilder()
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .build();

                //  http://api.openweathermap.org/data/2.5/group
                //  https://api.openweathermap.org/data/2.5/weather
                HttpUrl.Builder httpBuilder = HttpUrl.parse("http://api.openweathermap.org/data/2.5/group").newBuilder();
                httpBuilder.addQueryParameter("id",jobExecuteInfo.getJob().getParameter());
                httpBuilder.addQueryParameter("appid","c84c5f67c239af45375bb4a83d2a044a");
                System.out.println("execute job: " + jobExecuteInfo.getJob().getName() + " / planned execution time: "
                        + jobExecuteInfo.getPlanExecution() + " / real time: " + jobExecuteInfo.getRealExecution());
                Thread.sleep(3000);
                Request request = new Request.Builder().url(httpBuilder.build()).build();
                Response response = client.newCall(request).execute();
                JsonNode cityWeatherList = new ObjectMapper().readTree(response.body().string());
                for (JsonNode cityWeatheNode : cityWeatherList.get("list")) {
                    JsonNode t = cityWeatheNode.get("weather");
                    CityWeather cityWeather = new CityWeather();
                    cityWeather.setCity(cityWeatheNode.get("name").textValue());
                    cityWeather.setTemperature(cityWeatheNode.get("main").get("temp").asDouble());
                    cityWeather.setUnixEpochTime(cityWeatheNode.get("dt").asLong());
                    cityWeather.setWeatherDescription(cityWeatheNode.get("weather").get(0).get("description").textValue());
                    System.out.println(cityWeather.getCity());
                    RedisClusterUtil.setEx(cityWeather.getCity(), JsonUtil.obj2String(cityWeather), 1000);
                }
                jobExecuteResult.setEndTime(ZonedDateTime.now());
                jobExecuteResult.setOutput("done");
                jobExecuteResult.setError("None");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            jobLock.unlock();
        }
        return jobExecuteResult;
    }
}
