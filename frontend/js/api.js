const API = "http://localhost:8080/api";

function getToken() {
    return localStorage.getItem("token");
}

async function api(path, options = {}) {

    const headers = options.headers
        ? { ...options.headers }
        : {};

    if (!(options.body instanceof FormData)) {
        headers["Content-Type"] = "application/json";
    }

    if (getToken()) {
        headers["Authorization"] =
            "Bearer " + getToken();
    }

    const response = await fetch(
        API + path,
        {
            ...options,
            headers
        }
    );

    const data =
        await response.json().catch(() => ({}));

    if (response.status === 401) {
        localStorage.clear();
        location.href = "login.html";
    }

    if (!response.ok) {
        throw new Error(
            data.message || "Request failed."
        );
    }

    return data;
}
