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
                    agent.setReconnectDetail(m.hostname, m.port);
                    context.close();
                })

                .handle(MessageCommands.REGISTER_SUCCESS, context -> {
                    agent.registerLock.unlock();
                    log.info("Register successfully!");
                })

                .handle(MessageCommands.REGISTER_FAILED, context -> {
                    log.info("Register Failed!");
                    context.close();
                })

                .handle(MessageCommands.LOGIN_SUCCESS, context -> {
                    agent.loginLock.unlock();
                    log.info("Login successfully!");
                })

                .handle(MessageCommands.LOGIN_FAILED, context -> {
                    log.info("Login Failed!");
                    context.close();
                })

                .handle(MessageCommands.ACTIVITY_BROADCAST, context -> {
                    MsgActivityBroadcast m = context.read(MsgActivityBroadcast.class);
                    log.info("Rcv Broadcast: " + g.toJson(m.activity));
                    // todo: print to GUI
                    // todo: may need to apply filter of sending activity objects
                })

                .handle(MessageCommands.AUTHENTICATION_FAIL, context -> {
                    log.info("Activity broadcast authentication fail.");
                    context.close();
                })
                .handleError(context -> {

                });
    }
}
