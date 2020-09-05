<template>
    <v-dialog v-model="visible" width="800px">
        <v-form>
            <v-card>
                <v-card-title class="grey darken-2">{{modal=='add'?'添加连接':'修改连接'}}</v-card-title>
                <v-container>
                    <v-row class="mx-2">
                        <v-col cols="6">
                            <v-text-field :readonly="modal!='add'" v-model="name" prepend-icon="mdi-text"
                                placeholder="Name"></v-text-field>
                        </v-col>
                        <v-col cols="6">
                            <v-select prepend-icon="fa-database" :items="drivers" v-model="driver"></v-select>
                        </v-col>
                        <v-col cols="12">
                            <v-text-field v-model="url" prepend-icon="fa-link" placeholder="Url" :hint="hint">
                            </v-text-field>
                        </v-col>
                        <v-col cols="12">
                            <v-text-field v-model="username" prepend-icon="fa-user-circle" placeholder="Username">
                            </v-text-field>
                        </v-col>
                        <v-col cols="12">
                            <v-text-field v-model="password" type="password" prepend-icon="fa-lock"
                                placeholder="Password"></v-text-field>
                        </v-col>
                    </v-row>
                </v-container>
                <v-card-actions>
                    <v-spacer></v-spacer>
                    <v-btn text color="primary" @click="visible = false">取消</v-btn>
                    <v-btn text @click="save()">{{modal=='add'?'新增':'修改'}}</v-btn>
                </v-card-actions>
            </v-card>
        </v-form>
    </v-dialog>
</template>
<script>
    import connectConfig from "@/plugins/connectConfig"
    export default {
        name: "ConnectDialog",
        data() {
            return {
                storageName: "connects",
                drivers: ["mysql", "h2", "mssql"],
                name: "",
                modal: "add",
                visible: false,
                driver: "mysql",
                url: "",
                username: "",
                password: "",
            };
        },
        computed: {
            hint() {
                if (this.driver == "mysql") {
                    return "jdbc:mysql://{ip}:{port}/{db}?serverTimezone=GMT%2B8";
                } else if (this.driver == "h2") {
                    return "jdbc:h2:tcp://{ip}:{port}//{path}";
                } else if (this.driver == "mssql") {
                    return "jdbc:sqlserver://{ip}:{port};database={db}";
                } else {
                    return "";
                }
            }
        },
        methods: {
            show(name) {
                this.visible=true;
                let connects = JSON.parse(window.localStorage.getItem(this.storageName));
                if (connects && connects[name]) {
                    this.name = connects[name].name;
                    this.driver = connects[name].driver;
                    this.url = connects[name].url;
                    this.username = connects[name].username;
                    this.password = connects[name].password;
                    this.modal = 'update';
                } else {
                    this.name = '';
                    this.driver = 'mysql';
                    this.url = '';
                    this.username = '';
                    this.password = '';
                    this.modal = 'add';
                }

            },
            remove(name) {
                connectConfig.removeConnect(name);
                this.visible = false;
            },
            save() {
                if (this.modal == "add") {
                    this.add();
                } else {
                    this.edit();
                }
            },
            add() {
                connectConfig.addConnect({
                    name: this.name,
                    driver: this.driver,
                    url: this.url,
                    username: this.username,
                    password: this.password,
                });

                this.visible = false;
            },
            edit() {
                connectConfig.updateConnect({
                    name: this.name,
                    driver: this.driver,
                    url: this.url,
                    username: this.username,
                    password: this.password,
                });

                this.visible = false;
            },

        },
    }
</script>