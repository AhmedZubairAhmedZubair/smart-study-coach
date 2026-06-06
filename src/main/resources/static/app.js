async function generateStudy() {
    const topic = document.getElementById("topic").value;
    const difficulty = document.getElementById("difficulty").value;
    const resultDiv = document.getElementById("result");

    if (topic.trim() === "") {
        alert("Please enter a topic first");
        return;
    }

    resultDiv.innerHTML = "<p>Generating study material...</p>";

    try {
        const response = await fetch("/api/study/generate", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                topic: topic,
                difficulty: difficulty
            })
        });

        const data = await response.json();

        resultDiv.innerHTML = `
            <div class="card">
                <h2>${data.topic}</h2>
                <p><b>Difficulty:</b> ${data.difficulty}</p>

                <div class="section">
                    <h3>Summary</h3>
                    <p>${data.summary}</p>
                </div>

                <div class="section">
                    <h3>Step-by-Step Explanation</h3>
                    <p>${data.explanation}</p>
                </div>

                <div class="section">
                    <h3>MCQ Questions</h3>
                    ${data.mcqs.map((mcq, index) => `
                        <div class="mcq-box">
                            <p><b>Q${index + 1}:</b> ${mcq.question}</p>
                            <ul>
                                <li>A. ${mcq.optionA}</li>
                                <li>B. ${mcq.optionB}</li>
                                <li>C. ${mcq.optionC}</li>
                                <li>D. ${mcq.optionD}</li>
                            </ul>
                            <p><b>Correct Answer:</b> ${mcq.correctAnswer}</p>
                            <p><b>Weak Area:</b> ${mcq.weakArea}</p>
                        </div>
                    `).join("")}
                </div>

                <div class="section">
                    <h3>Practice Problems</h3>
                    <ul>
                        ${data.practiceProblems.map(problem => `<li>${problem}</li>`).join("")}
                    </ul>
                </div>
            </div>
        `;

    } catch (error) {
        resultDiv.innerHTML = "<p style='color:red;'>Something went wrong. Check backend console.</p>";
        console.log(error);
    }
}