public class MessageRouterRepresenter {
    private IMessageRouter representedRouter;

    final public IMessageRouter router() {
        return representedRouter;
    }

    final public void setRouter(IMessageRouter router) {
        representedRouter = router;
    }
}
