package orderManagement.services;

import com.google.gson.Gson;
import orderManagement.models.requests.PaymentRequest;
import orderManagement.models.responses.PaymentResponse;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class PaymentServiceClient {

    private static String paymentClientUrl;
    private final HttpClient client;
    private final Gson gson;

    public PaymentServiceClient() {
        this.client = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    public static void initialize(String payUrl){
        paymentClientUrl = payUrl;
    }

    public PaymentResponse makePayment(String auth, PaymentRequest paymentRequest) throws Exception {
        // Convert the payload object to JSON
        String jsonPayload = gson.toJson(paymentRequest); // you can use Jackson or Gson

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(paymentClientUrl))
                .header("Authorization", auth)   // forward the token
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Deserialize the response JSON into a PaymentResponse object
        return PaymentResponse.fromJson(response.body());
    }
}