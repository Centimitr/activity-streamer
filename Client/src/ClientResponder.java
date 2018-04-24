import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

abstract class ClientResponder extends Async {
    static final Logger log = LogManager.getLogger();
    static final Gson g = new Gson();
    Connectivity connectivity;
    ClientAgent agent = new ClientAgent();
    MessageRouter router = new MessageRouter();

    ClientResponder() {
        router
                .handle(MessageCommands.REDIRECT, context -> {
                    MsgRedirect m = context.read(MsgRedirect.class);
                    log.info("Will Redirect: " + m.hostname + " " + m.port);
                    agent.reconnect(m.hostname, m.port);
                    context.close();
                })

                .handle(MessageCommands.REGISTER_SUCCESS, context -> {
                    agent.registerLock.unlock();
                    log.info("Register successfully!");
                })

                .handle(MessageCommands.LOGIN_SUCCESS, context -> {
                    agent.loginLock.unlock();
                    log.info("Login successfully!");
                })

                .handle(MessageCommands.REGISTER_FAILED, context -> {
                    log.info("Register Failed!");
                    context.close();
                })

                .handle(MessageCommands.ACTIVITY_BROADCAST, context -> {
                    MsgActivityBroadcast m = context.read(MsgActivityBroadcast.class);
                    log.info("AM: " + g.toJson(m.activity));
                    System.out.println(g.toJson(m.activity));
                    // todo: may need to apply filter of sending activity objects
                })
                .handleError(context -> {

                });
    }
}
