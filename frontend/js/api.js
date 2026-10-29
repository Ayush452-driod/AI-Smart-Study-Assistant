const API_URL = "http://localhost:8080/api";


function getToken() {

    return localStorage.getItem("token");

}


async function apiRequest(endpoint, options = {}) {

    const headers = options.headers || {};


    /*
        Do not set Content-Type manually
        when sending FormData.
    */

    if (!(options.body instanceof FormData)) {

        headers["Content-Type"] =
            "application/json";

    }


    const token = getToken();

    if (token) {

        headers["Authorization"] =
            "Bearer " + token;

    }


    const response = await fetch(
        API_URL + endpoint,
        {
            ...options,
            headers: headers
        }
    );


    const data =
        await response.json()
        .catch(() => ({}));


    if (response.status === 401) {

        localStorage.clear();

        window.location.href =
            "login.html";

        return;

    }


    if (!response.ok) {

        throw new Error(
            data.message ||
            "Something went wrong."
        );

    }


    return data;
}