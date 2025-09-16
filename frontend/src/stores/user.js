import { defineStore } from 'pinia';
import axios from '../utils/axios';

export const useUserStore = defineStore('user', {
    state: () => ({
        id: null,
        name: null,
        email: null,
        isAuthenticated: false,
    }),
    actions: {
        async fetchProfile() {
            try {
                const res = await axios.get('/auth/profile', { withCredentials: true });
                this.name = res.data.name;
                this.email = res.data.email;
                this.isAuthenticated = true;
            } catch (err) {
                this.name = '';
                this.email = '';
                this.isAuthenticated = false;
                throw err;
            }
        },
        logout() {
            this.name = '';
            this.email = '';
            this.isAuthenticated = false;
        },
        setUser(user) {
            this.id = user.id
            this.name = user.name
            this.email = user.email
        },
        clearUser() {
            this.id = null
            this.name = null
            this.email = null
        }
    }
});
