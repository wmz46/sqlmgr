export default {
    storageName: "connects",
    addConnect(item) {
        let connects = JSON.parse(window.localStorage.getItem(this.storageName));
        let name = item.name;
        if (connects && connects[name]) {
            throw ("已存在名称为[" + name + "]的连接");
        } else {
            if (connects == null) {
                connects = {};
            }
            connects[name] = {
                name: item.name,
                driver: item.driver,
                url: item.url,
                username: item.username,
                password: item.password,
            };
            window.localStorage.setItem(this.storageName, JSON.stringify(connects));
            let event = new CustomEvent("updateConnects")
            window.dispatchEvent(event)
        }
    },
    updateConnect(item) {
        let name = item.name;
        let connects = JSON.parse(window.localStorage.getItem(this.storageName));
        if (connects && connects[name]) {
            connects[name] = {
                name: item.name,
                driver: item.driver,
                url: item.url,
                username: item.username,
                password: item.password,
            };
            window.localStorage.setItem(this.storageName, JSON.stringify(connects));
            let event = new CustomEvent("updateConnects")
            window.dispatchEvent(event)
        } else {
            throw ("没找到名称为[" + name + "]的连接");
        }
    },
    removeConnect(name) {
        let storageName = "connects";
        let connects = JSON.parse(window.localStorage.getItem(storageName));
        if (connects && connects[name]) {
            delete connects[name];
            window.localStorage.setItem(storageName, JSON.stringify(connects));
            let event = new CustomEvent("updateConnects")
            window.dispatchEvent(event)
        } else {
            throw ("没找到名称为[" + name + "]的连接");
        }
    },
    getConnects() {
        return JSON.parse(window.localStorage.getItem(this.storageName));
    }
}