# Smart Study Coach AI

An AI-powered study assistant that helps students learn any topic through 
auto-generated notes, quizzes, and personalized practice.

## What it does

- Enter any topic and difficulty level
- AI generates a detailed summary and explanation
- Generates MCQ quiz questions on the topic
- After quiz submission, detects your weak areas automatically
- Generates personalized practice questions based on what you got wrong
- Study history and quiz history saved permanently in database

## Tech Stack

- **Backend:** Java, Spring Boot
- **AI:** Google Gemini API
- **Database:** MySQL
- **Frontend:** HTML, CSS, JavaScript
- **Tools:** IntelliJ IDEA, Git

## How to run

1. Clone the repo
2. Get a free Gemini API key from [Google AI Studio](https://aistudio.google.com)
3. Create a MySQL database called `studycoachdb`
4. Update `application.properties` with your MySQL credentials and Gemini API key
5. Run the Spring Boot app
6. Open `http://localhost:8080` in your browser

## How the weak area detection works

After you submit a quiz, the app collects all wrong answers and their 
weak areas, then uses a Max Heap (Priority Queue) to rank them by frequency. 
The most repeated weak area gets targeted first in personalized practice.

## Why I built this

I built this to solve a real problem I face as a student — studying without 
knowing where your actual gaps are. This tool makes that automatic.
