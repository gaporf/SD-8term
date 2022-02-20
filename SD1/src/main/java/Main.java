import actor.ActorConfig;
import actor.MasterActor;
import actor.RequestMessage;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;
import server.ResponseFormat;
import server.SearchServer;
import server.ServerConfig;


import java.time.Duration;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        final ServerConfig yandexConfig = new ServerConfig("C:\\Users\\gapor\\ITMO\\SD_2term\\SD1\\src\\main\\resources\\yandex.conf");
        final SearchServer yandexServer = new SearchServer(yandexConfig);
        final ServerConfig googleConfig = new ServerConfig("C:\\Users\\gapor\\ITMO\\SD_2term\\SD1\\src\\main\\resources\\google.conf");
        final SearchServer googleServer = new SearchServer(googleConfig);
        final ServerConfig bingConfig = new ServerConfig("bing", 34371, "/q", ResponseFormat.JSON, 0);
        final SearchServer bingServer = new SearchServer(bingConfig);

        final ActorSystem system = ActorSystem.create("MySystem");
        final Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Enter your request");
            final String request = scanner.nextLine();
            if (request.equals("")) {
                break;
            }
            final ActorRef masterActor = system.actorOf(Props.create(MasterActor.class, new ActorConfig(system), List.of(yandexConfig, googleConfig, bingConfig)));
            final Timeout timeout = Timeout.create(Duration.ofDays(1));
            final Future<Object> future = Patterns.ask(masterActor, new RequestMessage(request), timeout);
            final String response;
            try {
                response = (String) Await.result(future, timeout.duration());
            } catch (final Exception e) {
                System.err.println("Can't get result: " + e.getMessage());
                break;
            }
            System.out.println(response);
            system.stop(masterActor);
        }
        System.out.println("Exiting...");
        yandexServer.stop();
        googleServer.stop();
        bingServer.stop();
        try {
            system.terminate();
            Await.ready(system.whenTerminated(), Timeout.create(Duration.ofSeconds(5)).duration());
        } catch (final Exception ignored) {
        }
    }
}
