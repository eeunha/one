import axios from 'axios';

const API_URL = 'http://localhost:8085/api/users';

export const getUsers = async () => {
    try {
        const response = await axios.get(API_URL);
        return response.data;
    } catch (error) {
        console.error('GET error:', error);
        return [];
    }
};

export const createUser = async (user) => {
    try {
        const response = await axios.post(API_URL, user);
        return response.data;
    } catch (error) {
        console.error('POST error:', error);
        return null;
    }
};
