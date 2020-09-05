<template>
    <v-list dense>
        <v-list-item link v-for="(item,i) in items" :key="i" :href="'#/db/'+item.text">
            <v-list-item-action>
                <v-icon>fa-link</v-icon>
            </v-list-item-action>
            <v-list-item-content>
                <v-list-item-title>{{item.text}}</v-list-item-title>
            </v-list-item-content>
            <v-list-item-action>
                <v-menu bottom left>
                    <template v-slot:activator="{ on, attrs }">
                        <v-btn icon v-bind="attrs" v-on="on">
                            <v-icon>mdi-dots-vertical</v-icon>
                        </v-btn>
                    </template>
                    <v-list>
                        <v-list-item @click="editConnect(item.text)">
                            <v-list-item-icon>
                                <v-icon>fa-edit</v-icon>
                            </v-list-item-icon>
                            <v-list-item-title>修改</v-list-item-title>
                        </v-list-item>
                        <v-list-item @click="removeConnect(item.text)">
                            <v-list-item-icon>
                                <v-icon>fa-remove</v-icon>
                            </v-list-item-icon>
                            <v-list-item-title>删除</v-list-item-title>
                        </v-list-item>
                    </v-list>
                </v-menu>
            </v-list-item-action>
        </v-list-item>
    </v-list>
</template>
<script>
    import ConnectConfig from "@/plugins/connectConfig"
    export default {
        data() {
            return {
                items: [],
            };
        },
        created() {
            this.updateMenu();
            window.addEventListener("updateConnects", this.updateMenu);
        },
        methods: {
            editConnect(name) {
                this.$emit("editConnect", name);
            },
            removeConnect(name) {
                this.$emit("removeConnect", name);
            },
            updateMenu() {
                this.items = [];
                let connects = ConnectConfig.getConnects();
                if (connects) {
                    for (let key in connects) {
                        this.items.push({
                            text: key
                        });
                    }
                }
            },
        },
    };
</script>