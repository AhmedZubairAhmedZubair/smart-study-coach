let currentMcqs = [];
let detectedWeakArea = "";
let currentSessionId = null;
let wrongWeakAreasForPractice = [];

async function generateStudy() {
    const topic = document.getElementById("topic").value;
    const difficulty = document.getElementById("difficulty").value;
    const mcqCount = document.getElementById("mcqCount").value;
    const customPrompt = document.getElementById("customPrompt").value;
    const resultDiv = document.getElementById("result");

    if (topic.trim() === "") {
        alert("Please enter a topic first");
        return;
    }

    if (mcqCount < 1) {
        alert("Please enter MCQ count greater than 0");
        return;
    }

    resultDiv.innerHTML = "<p>Generating detailed study material...</p>";

    try {
        const response = await fetch("/api/study/generate", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                topic: topic,
                difficulty: difficulty,
                mcqCount: mcqCount,
                customPrompt: customPrompt
            })
        });

        if (!response.ok) {
            const errorText = await response.text();
            console.log("Backend error:", errorText);
            resultDiv.innerHTML = `<p style="color:red;">Backend/Gemini error: ${errorText}</p>`;
            return;
        }

        const data = await response.json();

        currentSessionId = data.sessionId;
        currentMcqs = data.mcqs;
        wrongWeakAreasForPractice = [];

        resultDiv.innerHTML = `
            <div class="card">
                <h2>${data.topic}</h2>
                <p><b>Difficulty:</b> ${data.difficulty}</p>
                <p><b>Total MCQs:</b> ${data.mcqs.length}</p>

                <div class="section">
                    <h3>Summary</h3>
                    <p>${data.summary}</p>
                </div>

                <div class="section">
                    <h3>Detailed Explanation</h3>
                    <p>${data.explanation}</p>
                </div>

                <div class="section">
                    <h3>Quiz Questions</h3>

                    ${data.mcqs.map((mcq, index) => `
                        <div class="mcq-box">
                            <p><b>Q${index + 1}:</b> ${mcq.question}</p>

                            <label>
                                <input type="radio" name="question${index}" value="A">
                                A. ${mcq.optionA}
                            </label><br>

                            <label>
                                <input type="radio" name="question${index}" value="B">
                                B. ${mcq.optionB}
                            </label><br>

                            <label>
                                <input type="radio" name="question${index}" value="C">
                                C. ${mcq.optionC}
                            </label><br>

                            <label>
                                <input type="radio" name="question${index}" value="D">
                                D. ${mcq.optionD}
                            </label>
                        </div>
                    `).join("")}

                    <button onclick="submitQuiz()">Submit Quiz</button>
                </div>

                <div id="quizResult"></div>
                <div id="personalizedPractice"></div>

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

async function submitQuiz() {
    let score = 0;
    let wrongWeakAreas = [];

    for (let i = 0; i < currentMcqs.length; i++) {
        const selectedOption = document.querySelector(`input[name="question${i}"]:checked`);

        if (selectedOption === null) {
            alert("Please attempt all questions before submitting.");
            return;
        }

        const userAnswer = selectedOption.value;
        const correctAnswer = currentMcqs[i].correctAnswer;

        if (userAnswer === correctAnswer) {
            score++;
        } else {
            wrongWeakAreas.push(currentMcqs[i].weakArea);
        }
    }

    detectedWeakArea = detectWeakArea(wrongWeakAreas);
    wrongWeakAreasForPractice = wrongWeakAreas;

    document.getElementById("quizResult").innerHTML = `
        <div class="card">
            <h3>Quiz Result</h3>
            <p><b>Score:</b> ${score}/${currentMcqs.length}</p>
            <p><b>Wrong Answers:</b> ${wrongWeakAreas.length}</p>
            <p><b>Main Weak Area:</b> ${detectedWeakArea}</p>

            <button onclick="generatePersonalizedPractice()">
                Generate Priority-Based Practice
            </button>
        </div>
    `;

    try {
        const response = await fetch("/api/study/quiz-result", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                sessionId: currentSessionId.toString(),
                score: score.toString(),
                totalQuestions: currentMcqs.length.toString(),
                weakArea: detectedWeakArea
            })
        });

        if (response.ok) {
            console.log("Quiz result saved successfully");
        } else {
            console.log("Quiz result API error");
        }

    } catch (error) {
        console.log("Could not save quiz result", error);
    }
}

function detectWeakArea(wrongWeakAreas) {
    if (wrongWeakAreas.length === 0) {
        return "No weak area detected. Great job!";
    }

    let count = {};

    wrongWeakAreas.forEach(area => {
        count[area] = (count[area] || 0) + 1;
    });

    let mostRepeatedArea = wrongWeakAreas[0];

    for (let area in count) {
        if (count[area] > count[mostRepeatedArea]) {
            mostRepeatedArea = area;
        }
    }

    return mostRepeatedArea;
}

async function generatePersonalizedPractice() {
    const practiceDiv = document.getElementById("personalizedPractice");

    if (wrongWeakAreasForPractice.length === 0) {
        practiceDiv.innerHTML = `
            <div class="card">
                <h3>Personalized Practice</h3>
                <p>You performed well. No weak area detected.</p>
            </div>
        `;
        return;
    }

    practiceDiv.innerHTML = "<p>Generating priority-based personalized practice...</p>";

    try {
        const response = await fetch("/api/study/personalized-practice", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                weakAreas: wrongWeakAreasForPractice
            })
        });

        if (!response.ok) {
            const errorText = await response.text();
            practiceDiv.innerHTML = `<p style="color:red;">Practice generation error: ${errorText}</p>`;
            return;
        }

        const data = await response.json();

        practiceDiv.innerHTML = `
            <div class="card">
                <h3>Priority-Based Personalized Practice</h3>
                <p><b>Total Wrong Answers:</b> ${data.totalWrong}</p>

                <h4>Weak Topic Priority Ranking</h4>
                <ol>
                    ${data.priorityTopics.map(topic => `
                        <li>${topic.topic} — wrong ${topic.count} time(s)</li>
                    `).join("")}
                </ol>

                <h4>Practice Questions</h4>
                <ul>
                    ${data.practiceProblems.map(problem => `<li>${problem}</li>`).join("")}
                </ul>
            </div>
        `;

    } catch (error) {
        practiceDiv.innerHTML = "<p style='color:red;'>Could not generate personalized practice.</p>";
        console.log(error);
    }
}

async function loadHistory() {
    const historyDiv = document.getElementById("history");

    historyDiv.innerHTML = "<p>Loading history...</p>";

    try {
        const response = await fetch("/api/study/history");

        if (!response.ok) {
            historyDiv.innerHTML = "<p style='color:red;'>Study history API error.</p>";
            return;
        }

        const sessions = await response.json();

        if (sessions.length === 0) {
            historyDiv.innerHTML = "<p>No history found yet.</p>";
            return;
        }

        historyDiv.innerHTML = `
            <div class="card">
                <h3>Study History</h3>

                ${sessions.map(session => `
                    <div class="mcq-box">
                        <p><b>Topic:</b> ${session.topic}</p>
                        <p><b>Difficulty:</b> ${session.difficulty}</p>
                        <p><b>Summary:</b> ${session.summary}</p>
                        <p><b>Created At:</b> ${session.createdAt}</p>
                    </div>
                `).join("")}
            </div>
        `;

    } catch (error) {
        historyDiv.innerHTML = "<p style='color:red;'>Could not load history.</p>";
        console.log(error);
    }
}

async function loadQuizHistory() {
    const historyDiv = document.getElementById("history");

    historyDiv.innerHTML = "<p>Loading quiz history...</p>";

    try {
        const response = await fetch("/api/study/quiz-history");

        if (!response.ok) {
            historyDiv.innerHTML = "<p style='color:red;'>Quiz history API error.</p>";
            return;
        }

        const attempts = await response.json();

        if (attempts.length === 0) {
            historyDiv.innerHTML = "<p>No quiz history found yet. Submit a quiz first.</p>";
            return;
        }

        historyDiv.innerHTML = `
            <div class="card">
                <h3>Quiz History</h3>

                ${attempts.map(attempt => `
                    <div class="mcq-box">
                        <p><b>Session ID:</b> ${attempt.sessionId}</p>
                        <p><b>Score:</b> ${attempt.score}/${attempt.totalQuestions}</p>
                        <p><b>Weak Area:</b> ${attempt.weakArea}</p>
                        <p><b>Created At:</b> ${attempt.createdAt}</p>
                    </div>
                `).join("")}
            </div>
        `;

    } catch (error) {
        historyDiv.innerHTML = "<p style='color:red;'>Could not load quiz history.</p>";
        console.log(error);
    }
}