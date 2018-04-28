abstract class MessageRouterRepresenter {
    private IMessageRouter representedRouter;

    final IMessageRouter router() {
        return representedRouter;
    }

    final void setRouter(IMessageRouter router) {
        representedRouter = router;
    }
}
