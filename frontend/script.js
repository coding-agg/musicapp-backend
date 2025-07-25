async function searchVideos() {
    const query = document.getElementById("searchInput").value;
    const response = await fetch("http://localhost:5001/search", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ query })
    });

    const data = await response.json();
    const resultsList = document.getElementById("results");
    resultsList.innerHTML = "";

    data.forEach(video => {
        const li = document.createElement("li");
        li.innerHTML = `<h3>${video.title}</h3>
        <img src="${video.thumbnail}" alt="Thumbnail" width="300">
        <p><a href="${video.url}" target="_blank">Watch</a></p>
        <button onclick="downloadAudio('${video.url}')">Download</button>`;
        resultsList.appendChild(li);
    });
}
function downloadAudio(url) {
    fetch("http://localhost:5001/download", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ url })
    })
        .then(response => response.json())
        .then(data => {
            if (data.file) {
                // This is the correct download link
                const downloadLink = `http://localhost:5001/downloads/${encodeURIComponent(data.file)}`;
                window.open(downloadLink, '_blank');  // Opens the download in a new tab
            } else {
                console.error("Download failed", data);
            }
        })
        .catch(error => {
            console.error("Error downloading file:", error);
        });
}
