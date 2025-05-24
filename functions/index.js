/**
 * Import function triggers from their respective submodules:
 *
 * const {onCall} = require("firebase-functions/v2/https");
 * const {onDocumentWritten} = require("firebase-functions/v2/firestore");
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

const {onRequest} = require("firebase-functions/v2/https");
const logger = require("firebase-functions/logger");

// Create and deploy your first functions
// https://firebase.google.com/docs/functions/get-started

// exports.helloWorld = onRequest((request, response) => {
//   logger.info("Hello logs!", {structuredData: true});
//   response.send("Hello from Firebase!");
// });

const functions = require("firebase-functions");
const axios = require("axios");

// Your OpenAI API key (keep this secret here)
const OPENAI_API_KEY = "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";

exports.generateQuizCall = functions.https.onCall(async (data, context) => {
  const content = data.text;
  const formats = data.formats; // { MCQ: 3, TF: 2, ... }

  if (!content) {
    throw new functions.https.HttpsError('invalid-argument', 'Text content is required.');
  }

  // Build prompt string based on formats
  let prompt = "You are a quiz generator. Based on the following content, generate ";
  for (const [format, count] of Object.entries(formats)) {
    prompt += `${count} ${format} question${count > 1 ? "s" : ""}, `;
  }
  prompt += `in this format:\n- Question\n- Choices (if MCQ)\n- Correct answer\n\nContent:\n${content}\n\nGenerate the questions now.`;

  try {
    const response = await axios.post(
      "https://api.openai.com/v1/chat/completions",
      {
        model: "gpt-3.5-turbo",
        messages: [
          { role: "system", content: "You are a helpful assistant that generates quizzes." },
          { role: "user", content: prompt }
        ],
        max_tokens: 1000,
      },
      {
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${OPENAI_API_KEY}`,
        },
      }
    );

    const quizText = response.data.choices[0].message.content;
    return { quiz: quizText };
  } catch (error) {
    console.error("OpenAI API error:", error.response?.data || error.message);
    throw new functions.https.HttpsError('internal', 'Failed to generate quiz.');
  }
});

