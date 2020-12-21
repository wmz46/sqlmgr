<template>
    <v-dialog v-model="visible" width="960px">
        <v-form>
            <v-card>
                <v-card-title class="grey darken-2">SQL转换</v-card-title>
                <v-container>
                    <v-row class="mx-2">
                        <v-col cols="6">
                            <v-textarea label="mysql" id="sql1" v-model="sql1" hint="请输入sql" rows="20">
                            </v-textarea>
                        </v-col>
                        <v-col cols="6">
                            <v-textarea label="达梦" id="sql2" v-model="sql2" hint=""  rows="20">
                            </v-textarea>
                        </v-col>
                    </v-row>
                </v-container>
                <v-card-actions>
                    <v-spacer></v-spacer>
                    <v-btn text color="primary" @click="visible = false">取消</v-btn>
                    <v-btn text @click="translate()">转换</v-btn>
                </v-card-actions>
            </v-card>
        </v-form>
    </v-dialog>
</template>
<script>
    import config from '@/plugins/config'
    export default {
        name: "TranslateDialog",
        data() {
            return {
                visible: false,
                editor1: null,
                editor2: null,
                sql1: '',
                sql2: '',
            };
        },

        methods: {
            translate() {
                let that = this;
                let sql = that.sql1;
                let data = new FormData();
                data.append('sql', sql)
                this.$axios.post(config.apiUrl + '/api/mysql2dm', data).then(res => {
                    if (res.data.success) {
                        that.sql2 = res.data.data;
                    } else {
                        that.$throw(res.data.message);
                    }
                }).catch(err => {
                    that.$throw(err)
                })
            },
            show() {
                this.visible = true
            },

        },
    }
</script>