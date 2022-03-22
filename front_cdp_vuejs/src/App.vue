<template>
  <div id="app">
    <div class="page-header">
      <h1><i class="fa fa-event" aria-hidden="true"></i> My Event</h1>
    </div>
    <img alt="Vue logo" src="./assets/logo.png" />
    <hello-world msg="Welcome to Your Vue.js App" />
  </div>
</template>

<script lang="ts">
import { Component, Vue } from "vue-property-decorator";

import helloWorld from "./components/HelloWorld.vue";
import viewEvents from "./views/ViewEvents.vue";

@Component({components:{helloWorld,viewEvents}})
export default class App extends Vue {  

}
</script>

<style>
@import url('https://fonts.googleapis.com/css?family=Lobster|Open+Sans+Condensed:300|Roboto:500');

#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  margin-top: 60px;
}
.page-header h1 {
    font-family: 'Lobster', cursive;
    color: #4c4c4c;
}
</style>
