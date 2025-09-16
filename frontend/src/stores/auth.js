import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useAuthStore = defineStore('auth', () => {
    // 상태 (State)
    const accessToken = ref(null);
    const user = ref(null);

    // 액션 (Actions)
    const setLoginInfo = (token, userData) => {
        accessToken.value = token;
        user.value = userData;
    };

    const clearLoginInfo = () => {
        accessToken.value = null;
        user.value = null;
    };

    // 상태와 액션을 반환
    return { accessToken, user, setLoginInfo, clearLoginInfo };
});