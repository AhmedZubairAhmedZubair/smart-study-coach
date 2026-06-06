let currentMcqs = [];

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

        currentMcqs = data.mcqs;

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

function submitQuiz() {
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

    const weakArea = detectWeakArea(wrongWeakAreas);

    document.getElementById("quizResult").innerHTML = `
        <div class="card">
            <h3>Quiz Result</h3>
            <p><b>Score:</b> ${score}/${currentMcqs.length}</p>
            <p><b>Weak Area:</b> ${weakArea}</p>
        </div>
    `;
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