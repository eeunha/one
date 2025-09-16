import axios from 'axios';

// 기본 axios 인스턴스 설정
const instance = axios.create({
    baseURL: 'http://localhost:8085/api',
    withCredentials: true, // 쿠키 전송 허용
    headers: {
        'Content-Type': 'application/json',
    },
});

// 응답 인터셉터
instance.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response && (error.response.status === 401 || error.response.status === 403)) {
            const confirmLogin = window.confirm('로그인이 필요합니다. 로그인 페이지로 이동하시겠습니까?');
            if (confirmLogin) {
                window.location.href = '/login';
            } else {
                window.location.href = '/';
            }
        }
        return Promise.reject(error);
    }
);

export default instance;
