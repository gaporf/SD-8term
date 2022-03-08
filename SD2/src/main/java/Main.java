import io.reactivex.netty.protocol.http.server.HttpServer;
import mongo.Mongo;
import rx.Observable;
import server.ServerUtils;

public class Main {
    public static void main(final String[] args) {
        final Mongo mongo = new Mongo("mongodb://localhost:27017");
        HttpServer
                .newServer(8080)
                .start((req, resp) -> {
                    final Observable<String> response = ServerUtils.handle(mongo, req);
                    return resp.writeString(response);
                })
                .awaitShutdown();
    }
}
