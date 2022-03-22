<template>
  <v-app>
    <v-app-bar app color="primary" dark>
      <v-app-bar-nav-icon></v-app-bar-nav-icon>
      <div class="d-flex align-center">
        <v-toolbar-title class="page-title"> My Event </v-toolbar-title>
      </div>

      <v-spacer></v-spacer>
      <v-btn icon to="/events">
        <v-icon>mdi-heart</v-icon>
      </v-btn>
    </v-app-bar>

    <v-main>
      <nav>
        <router-link to="/">Home</router-link> |
        <router-link to="/events">Events</router-link> |
        <router-link to="/about">About</router-link>
      </nav>
      <router-view />
    </v-main>
  </v-app>
</template>

<script lang="ts">
import Vue from "vue";


export default Vue.extend({
  name: "App",


});
</script>
