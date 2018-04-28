import {decorate, observable, action} from "mobx";

const getAgent = (fn) => {
    const agent = window['devbycmagent'];
    if (agent) {
        alert(!!agent);
        fn(agent);
    }
};

class Stream {
    showLoading = true;

    showRegister = false;
    registerSuccess = true;

    showLogIn = false;
    loggedInSuccess = false;
    showLogOut = false;
    reconnectFlag = false;

    hostname = "";
    port = "";
    username = "";
    secret = "";
    messages = [];

    draft = "";
    valid = false;

    setLoaded = (needRegister, username, secret) => {
        this.showLoading = false;
        this.username = username;
        this.secret = secret;
        this.showRegister = needRegister;
        if (needRegister) {
            this.showRegister = false;
            this.showLogIn = true;
        }
    };

    setRegistered = (success, username, secret) => {
        this.username = username;
        this.secret = secret;
        // this.registerSuccess = success;
        if (success) {
            this.showRegister = false;
            this.showLogIn = true;
        }
    };

    setLoggedIn = (success, hostname, port) => {
        this.hostname = hostname;
        this.port = port;
        if (success) {
            this.showLogIn = false;
            this.reconnectFlag = true;
            this.messages.push({
                type: 'notification',
                content: `Server ${this.hostname}:${this.port} connected.`
            });
        }
    };

    logout = () => {
        this.showLogOut = true;
        getAgent(agent => agent.logout())
    };

    check = () => {
        const checkJsonObject = str => {
            if (!str || !str.length) return false;
            try {
                JSON.parse(str.toString());
                return true;
            } catch (e) {
            }
            return false;
        };
        const ok = checkJsonObject(this.draft);
        this.valid = ok;
        return ok;
    };

    send = () => {
        const ok = this.check();
        if (ok) {
            console.log("Send:", this.draft);
            getAgent(agent => agent.send(this.draft));
            this.draft = "";
            this.check();
        }
    };

    formatDraft = () => {
        // todo: format
        const ok = this.check();
        if (ok) {
            // this.draft = JSON.stringify(this.draft, null, ' ');
        }
    };

    clearMessages = () => {
        this.messages = [];
    };

    addMessage = message => {
        const m = JSON.parse(message);
        const username = m['authenticated_user'];
        delete m['authenticated_user'];
        const msg = {
            type: 'message',
            username,
            content: JSON.stringify(m, null, `  `)
        };
        this.messages.push(msg);
    };
}

decorate(Stream, {
    showLoading: observable,
    showRegister: observable,
    registerSuccess: observable,
    showLogIn: observable,
    reconnectFlag: observable,
    firstTimeLogIn: observable,
    loggedInSuccess: observable,
    showLogOut: observable,
    hostname: observable,
    port: observable,
    username: observable,
    secret: observable,
    messages: observable,
    draft: observable,
    valid: observable,
    setLoaded: action,
    setRegistered: action,
    setLoggedIn: action,
    logout: action,
    check: action,
    send: action,
    formatDraft: action,
    clearMessages: action,
    addMessage: action,
});

const stream = new Stream();
export default stream;

window['devbycmstream'] = stream;

// setTimeout(() => {
//     stream.setLoaded(true, 'xiaoming2', 'fds6f78dsa6f78as6f78sa');
//     setTimeout(() => {
//         stream.setRegistered(true, 'xiaoming2', 'fds6f78dsa6f78as6f78sa');
//         setTimeout(() => {
//             stream.setLoggedIn(true);
//             setTimeout(() => {
//                 stream.addMessage(JSON.stringify({
//                     authenticated_user: "anonymouse",
//                     info: "hello xiaoming!",
//                     note: "blablabla?"
//                 }));
//                 setTimeout(() => {
//                     stream.addMessage(JSON.stringify({
//                         authenticated_user: "anonymouse",
//                         info: "hello xiaoming!",
//                         note: "blablabla?"
//                     }));
//                     setTimeout(() => {
//                         stream.addMessage(JSON.stringify({
//                             authenticated_user: "anonymouse",
//                             info: "hello xiaoming!",
//                             note: "blablabla?"
//                         }));
//                         setTimeout(() => {
//                             stream.addMessage(JSON.stringify({
//                                 authenticated_user: "anonymouse",
//                                 info: "hello xiaoming!",
//                                 note: "blablabla?"
//                             }));
//                         }, 500);
//                     }, 500);
//                 }, 500);
//             }, 500);
//         }, 500);
//     }, 500);
// }, 1000);