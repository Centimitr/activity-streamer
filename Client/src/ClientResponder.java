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
                    agent.eval(String.format("setRegistered(%s, '%s', '%s')",
                            true,
                            Settings.getUsername(),
                            Settings.getSecret()
                    ));
                })

                .handle(MessageCommands.REGISTER_FAILED, context -> {
                    log.info("Register Failed!");
                    context.close();
                    agent.eval(String.format("setRegistered(%s, '%s', '%s')",
                            false,
                            Settings.getUsername(),
                            Settings.getSecret()
                    ));
                })

                .handle(MessageCommands.LOGIN_SUCCESS, context -> {
                    agent.loginLock.unlock();
                    log.info("Login successfully!");
                    agent.eval(String.format("setLoggedIn(%s, '%s', '%s')", true, Settings.getRemoteHostname(), Settings.getRemotePort()));
                })

                .handle(MessageCommands.LOGIN_FAILED, context -> {
                    log.info("Login Failed!");
                    agent.eval(String.format("setLoggedIn(%s, '%s', '%s')", false, Settings.getRemoteHostname(), Settings.getRemotePort()));
                    context.close();
                })

                .handle(MessageCommands.ACTIVITY_BROADCAST, context -> {
                    MsgActivityBroadcast m = context.read(MsgActivityBroadcast.class);
                    log.info("Rcv Broadcast: " + g.toJson(m.activity));
                    agent.eval(String.format("addMessage('%s')", g.toJson(m.activity)));
                })

                .handle(MessageCommands.INVALID_MESSAGE, context -> {
                    MsgInvalidMessage m = context.read(MsgInvalidMessage.class);
                    log.info("Rcv: Invalid Message: " + g.toJson(m.info));
                    context.close();
                })

                .handle(MessageCommands.AUTHENTICATION_FAIL, context -> {
                    log.info("Activity broadcast authentication fail.");
                    context.close();
                })
                .handleError((context,error) -> {
                    String info;
                    switch (error) {
                        case "Parse Error":
                            info = "Json Parse Error while parsing message.";
                            break;
                        case "Syntax Error":
                            info = "Message Syntax Error.";
                            break;
                        default:
                            info = "Command not support.";
                            break;
                    }
                    MsgInvalidMessage res = new MsgInvalidMessage(info);
                    context.write(res);
                    context.close();
                });
    }
}
