// WeatherApp.java

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;

// You'll need to add the org.json library to your project.
// If you are using Maven, add this to your pom.xml:
// <dependency>
//     <groupId>org.json</groupId>
//     <artifactId>json</artifactId>
//     <version>20240303</version>
// </dependency>
// If you are using Gradle, add this to your build.gradle:
// implementation 'org.json:json:20240303'
// latitude 28.7041
// longitude 77.1025
import org.json.JSONArray;
import org.json.JSONObject;

public class WeatherApp {

    // Open-Meteo API does not require an API key for basic forecasts.
    private static final String BASE_URL = "https://api.open-meteo.com/v1/forecast";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter latitude (e.g., 52.52): ");
        String latitudeStr = scanner.nextLine();
        System.out.print("Enter longitude (e.g., 13.41): ");
        String longitudeStr = scanner.nextLine();
        scanner.close();

        try {
            double latitude = Double.parseDouble(latitudeStr);
            double longitude = Double.parseDouble(longitudeStr);

            // Build the URL for the API request
            // We request hourly temperature_2m data
            String apiUrl = String.format("%s?latitude=%.2f&longitude=%.2f&hourly=temperature_2m", BASE_URL, latitude, longitude);

            // Create an HttpClient instance
            HttpClient client = HttpClient.newHttpClient();

            // Create an HttpRequest
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .build();

            // Send the request and get the response
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Check if the request was successful (status code 200)
            if (response.statusCode() == 200) {
                // Parse the JSON response
                JSONObject jsonResponse = new JSONObject(response.body());

                // Extract and display relevant weather data
                displayHourlyTemperatureData(jsonResponse, latitude, longitude);
            } else {
                System.err.println("Error fetching weather data. Status code: " + response.statusCode());
                System.err.println("Response body: " + response.body());
            }

        } catch (NumberFormatException e) {
            System.err.println("Invalid latitude or longitude format. Please enter numeric values.");
        } catch (IOException | InterruptedException e) {
            System.err.println("An error occurred during the HTTP request: " + e.getMessage());
            e.printStackTrace();
        } catch (org.json.JSONException e) {
            System.err.println("Error parsing JSON response: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Parses the JSON response from Open-Meteo and displays the hourly temperature data.
     * @param jsonResponse The JSONObject containing the hourly temperature data.
     * @param latitude The latitude used for the request.
     * @param longitude The longitude used for the request.
     */
    private static void displayHourlyTemperatureData(JSONObject jsonResponse, double latitude, double longitude) {
        System.out.println("\n--- Hourly Temperature Data ---");
        System.out.printf("For Coordinates: Latitude %.2f, Longitude %.2f\n", latitude, longitude);

        if (jsonResponse.has("hourly")) {
            JSONObject hourly = jsonResponse.getJSONObject("hourly");

            if (hourly.has("time") && hourly.has("temperature_2m")) {
                JSONArray times = hourly.getJSONArray("time");
                JSONArray temperatures = hourly.getJSONArray("temperature_2m");

                System.out.println("Time (UTC) | Temperature (°C)");
                System.out.println("------------------------------");

                // Display up to the next 24 hours of data for brevity, or all available data
                int displayLimit = Math.min(times.length(), 24); // Displaying next 24 hours or less if not available

                for (int i = 0; i < displayLimit; i++) {
                    String time = times.getString(i);
                    double temperature = temperatures.getDouble(i);
                    System.out.printf("%s | %.1f\n", time, temperature);
                }

                if (times.length() > displayLimit) {
                    System.out.println("...");
                    System.out.printf("(Showing first %d hours out of %d total hours)\n", displayLimit, times.length());
                }

            } else {
                System.out.println("No 'time' or 'temperature_2m' data found in the hourly forecast.");
            }
        } else {
            System.out.println("No 'hourly' data found in the response.");
        }
        System.out.println("------------------------------");
    }
}
