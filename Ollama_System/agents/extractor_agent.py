from typing import Dict, Any
from pdfminer.high_level import extract_text # pip install pdfminer.six
from .base_agent import BaseAgent

class ExtractorAgent(BaseAgent):
    def __init__(self):
        super().__init__(
            name="Extractor",
            instructions="""Extract and structure information from resumes.
            Focus on: personal info, work experience, education, skills, and certifications.
            Provide output in a clear, structured format."""
        )
    
    async def run(self, messages: list) -> Dict[str, Any]:
        """Process the resume and extract information"""
        print("📄 Extractor: Processing resume")
        
        resume_data = eval(messages[-1]["content"])
        
        # Extract text from PDF
        if resume_data.get("file_path"):
            raw_text = extract_text(resume_data["file_path"])
        else:
            raw_text = resume_data.get("text", "")

        # Get structured information from Ollama
        extracted_info = self._query_ollama(raw_text)

        return {
            "raw_text": raw_text,
            "structured_data": extracted_info,
            "extraction_status": "completed"
        }



# import json
# from typing import Dict, Any
# from swarm import Agent, Swarm
# from openai import OpenAI
# import PyPDF2  # pip install pypdf2
# from PyPDF2 import PdfReader  # Updated to use PdfReader


# ollama_client = OpenAI(
#     base_url="http://localhost:11434/v1",
#     api_key="ollama",  # required, but unused
# )

# # Initialize Swarm client
# client = Swarm(client=ollama_client)


# # Extractor agent: Extracts relevant information from the resume PDF
# def extractor_agent_function(pdf_path: str) -> Dict[str, Any]:
#     # Read and extract text from the PDF file
#     with open(pdf_path, "rb") as pdf_file:
#         reader = PdfReader(pdf_file)
#         resume_text = ""
#         for page in reader.pages:
#             resume_text += page.extract_text() + "\n"

#             # client = OpenAI(
#             #     base_url="http://localhost:11434/v1",
#             #     api_key="ollama",  # required, but unused
#             # )

#     # response = client.chat.completions.create(
#     #   model="llama2",
#     #   messages=[
#     #     {"role": "system", "content": "You are a helpful assistant."},
#     #     {"role": "user", "content": "Who won the world series in 2020?"},
#     #     {"role": "assistant", "content": "The LA Dodgers won in 2020."},
#     #     {"role": "user", "content": "Where was it played?"}
#     #   ]
#     # )
#     # print(response.choices[0].message.content)

#     print(f"Extracted text from PDF:{resume_text}")
#     response = ollama_client.chat.completions.create(
#         model="llama3.2",
#         messages=[
#             {
#                 "role": "user",
#                 "content": """transform this content: \n{resume_text}\n into a JSON format.
#                 Make sure to extract the important information from the resume. and provide it in JSON format.
#                 DO NOT write python code or script. Just provide the extracted information in JSON format.
#                 Don't include greetings or any other unnecessary information, just the JSON data.
#                 Do not include: 'Here is the transformed content in JSON format' or any other similar phrases.""",
#             }
#         ],
#     )
#     print(response)
#     # Accessing the completion message properly
#     extracted_data_str = response.choices[0].message.content.strip()
#     extracted_data = json.loads(extracted_data_str)
#     print(f"Extracted data: {extracted_data}")

#     return extracted_data


# extractor_agent = Agent(
#     name="Extractor Agent",
#     model="llama3.2",
#     instructions="Extract relevant information from resumes and provide it in JSON format.",
#     functions=[extractor_agent_function],
# )
