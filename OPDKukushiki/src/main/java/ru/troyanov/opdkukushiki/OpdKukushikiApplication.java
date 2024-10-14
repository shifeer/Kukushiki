package ru.troyanov.opdkukushiki;

import jakarta.annotation.PostConstruct;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;


@SpringBootApplication
public class OpdKukushikiApplication {

    @Autowired
    private RabbitAdmin rabbitAdmin;
    @Autowired
    private Queue queue;

    @PostConstruct
    public void init() {
        rabbitAdmin.declareQueue(queue);
    }

//    private static final String API_KEY = "5e16b0c2ccaf2f88d136576077bf220c0a605fdc6f2c7b8c765f8aedc2b519e057d4c06ac5c3f757a5db6032ce4f8a362e2e0a9f644223c5ebded9b0ce40cf56"; // your API key
//    private static final String LANGUAGE = "ru-RU"; // language spoken in the audio file
//    private static final String FILE_PATH = "user/example.mp3"; // local file path of the audio file

    public static void main(String[] args) {
        SpringApplication.run(OpdKukushikiApplication.class, args);
//
//
//            String fileName = Paths.get(FILE_PATH).getFileName().toString();
//            String url = "https://api.transkriptor.com/1/Upload";
//
//            // first request to get the presigned url through which we will upload our audio file
//            try (CloseableHttpClient client = HttpClients.createDefault()) {
//                HttpGet request = new HttpGet(url + "?apiKey=" + API_KEY + "&language=" + LANGUAGE + "&fileName=" + fileName);
//                HttpResponse response = client.execute(request);
//                String responseString = EntityUtils.toString(response.getEntity());
//                if (response.getStatusLine().getStatusCode() != 200) {
//                    throw new Exception(responseString);
//                }
//
//                JSONObject jsonResponse = new JSONObject(responseString);
//                url = jsonResponse.getString("url");
//                String givenOrderId = jsonResponse.getJSONObject("fields").getString("key").split("-+-")[0];
//
//                // attach the audio file
//                File file = new File(FILE_PATH);
//                HttpEntity entity = MultipartE.create()
//                        .addTextBody("key", jsonResponse.getJSONObject("fields").getString("key"))
//                        .addTextBody("AWSAccessKeyId", jsonResponse.getJSONObject("fields").getString("AWSAccessKeyId"))
//                        .addTextBody("x-amz-security-token", jsonResponse.getJSONObject("fields").getString("x-amz-security-token"))
//                        .addTextBody("policy", jsonResponse.getJSONObject("fields").getString("policy"))
//                        .addTextBody("signature", jsonResponse.getJSONObject("fields").getString("signature"))
//                        .addBinaryBody("file", file)
//                        .build();
//
//                // upload the audio file and initiate the transcription
//                HttpPost postRequest = new HttpPost(url);
//                postRequest.setEntity(entity);
//                HttpResponse postResponse = client.execute(postRequest);
//
//                System.out.println(postResponse.getStatusLine().getStatusCode());
//                System.out.println(givenOrderId);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//
    }
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}