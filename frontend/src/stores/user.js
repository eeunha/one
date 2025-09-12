import { defineStore } from 'pinia';
import axios from '../utils/axios';

export const useUserStore = defineStore('user', {
    state: () => ({
        name: '',
        email: '',
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
        }
    }
});
