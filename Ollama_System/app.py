# == Command Line Interface (CLI) ==
# == to run use: python3 app.py resumes/sample_resume.pdf ==
import asyncio
import os
import sys
from datetime import datetime
from rich.console import Console
from rich.panel import Panel
from rich.progress import Progress, SpinnerColumn, TextColumn
from rich.table import Table
from agents.orchestrator import OrchestratorAgent
from utils.logger import setup_logger
from utils.exceptions import ResumeProcessingError

# Initialize Rich console for beautiful CLI output
console = Console()
logger = setup_logger()


async def process_resume(file_path: str) -> None:
 """Process a resume through the AI recruitment pipeline"""
 try:
     # Validate input file
     if not os.path.exists(file_path):
         raise FileNotFoundError(f"Resume not found at {file_path}")

     if not file_path.lower().endswith(".pdf"):
         raise ValueError("Please provide a PDF resume file")

     logger.info(f"Starting recruitment process for: {os.path.basename(file_path)}")

     # Display welcome banner
     console.print(
         Panel.fit(
             "[bold blue]AI Recruitment Agency[/bold blue]\n"
             "[dim]Powered by Ollama (llama2) and Swarm Framework[/dim]",
             border_style="blue",
         )
     )

     # Initialize orchestrator
     orchestrator = OrchestratorAgent()

     # Prepare resume data
     resume_data = {
         "file_path": file_path,
         "submission_timestamp": datetime.now().isoformat(),
     }

     # Process with progress indication
     with Progress(
         SpinnerColumn(),
         TextColumn("[progress.description]{task.description}"),
         console=console,
     ) as progress:
         task = progress.add_task("[cyan]Processing resume...", total=None)
         result = await orchestrator.process_application(resume_data)
         progress.update(task, completed=True)

     if result["status"] == "completed":
         logger.info("Resume processing completed successfully")

         # Create results table
         table = Table(
             title="Analysis Summary", show_header=True, header_style="bold magenta"
         )
         table.add_column("Category", style="cyan")
         table.add_column("Details", style="white")

         # Add analysis results
         table.add_row(
             "Skills Analysis", str(result["analysis_results"]["skills_analysis"])
         )
         table.add_row(
             "Confidence Score",
             f"{result['analysis_results']['confidence_score']:.2%}",
         )

         console.print("\n", table)

         # Display job matches
         matches_table = Table(
             title="Job Matches", show_header=True, header_style="bold green"
         )
         matches_table.add_column("Position", style="cyan")
         matches_table.add_column("Match Score", style="white")
         matches_table.add_column("Location", style="white")

         for job in result["job_matches"]["matched_jobs"]:
             matches_table.add_row(
                 job["title"],
                 f"{job.get('match_score', 'N/A')}",
                 job.get("location", "N/A"),
             )

         console.print("\n", matches_table)

         # Display screening results
         console.print(
             Panel(
                 f"[bold]Screening Score:[/bold] {result['screening_results']['screening_score']}%\n\n"
                 f"{result['screening_results']['screening_report']}",
                 title="Screening Results",
                 border_style="green",
             )
         )

         # Display final recommendation
         console.print(
             Panel(
                 result["final_recommendation"]["final_recommendation"],
                 title="Final Recommendation",
                 border_style="blue",
             )
         )

         # Save results to file
         output_dir = "results"
         if not os.path.exists(output_dir):
             os.makedirs(output_dir)

         output_file = os.path.join(
             output_dir, f"analysis_{datetime.now().strftime('%Y%m%d_%H%M%S')}.txt"
         )

         with open(output_file, "w") as f:
             f.write("AI Recruitment Analysis Results\n")
             f.write("=" * 50 + "\n\n")
             f.write(f"Resume: {os.path.basename(file_path)}\n")
             f.write(f"Date: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n\n")
             f.write("Analysis Summary\n")
             f.write("-" * 20 + "\n")
             f.write(
                 f"Skills Analysis: {result['analysis_results']['skills_analysis']}\n"
             )
             f.write(
                 f"Confidence Score: {result['analysis_results']['confidence_score']:.2%}\n\n"
             )
             f.write("Job Matches\n")
             f.write("-" * 20 + "\n")
             for job in result["job_matches"]["matched_jobs"]:
                 f.write(f"\nPosition: {job['title']}\n")
                 f.write(f"Match Score: {job.get('match_score', 'N/A')}\n")
                 f.write(f"Location: {job.get('location', 'N/A')}\n")
             f.write("\nScreening Results\n")
             f.write("-" * 20 + "\n")
             f.write(f"Score: {result['screening_results']['screening_score']}%\n")
             f.write(
                 f"Report: {result['screening_results']['screening_report']}\n\n"
             )
             f.write("Final Recommendation\n")
             f.write("-" * 20 + "\n")
             f.write(str(result["final_recommendation"]["final_recommendation"]))

         console.print(f"\n[green]✓[/green] Results saved to: {output_file}")

     else:
         error_msg = f"Process failed at stage: {result['current_stage']}"
         if "error" in result:
             error_msg += f"\nError: {result['error']}"
         logger.error(error_msg)
         console.print(f"\n[red]✗[/red] {error_msg}")

 except FileNotFoundError as e:
     logger.error(f"File error: {str(e)}")
     console.print(f"[red]Error:[/red] {str(e)}")
 except ValueError as e:
     logger.error(f"Validation error: {str(e)}")
     console.print(f"[red]Error:[/red] {str(e)}")
 except ResumeProcessingError as e:
     logger.error(f"Processing error: {str(e)}")
     console.print(f"[red]Error:[/red] {str(e)}")
 except Exception as e:
     logger.error(f"Unexpected error: {str(e)}", exc_info=True)
     console.print(f"[red]✗ An unexpected error occurred:[/red] {str(e)}")


def main():
 """Main entry point for the AI recruitment system"""
 import argparse

 parser = argparse.ArgumentParser(
     description="AI Recruitment Agency - Resume Processing System",
     formatter_class=argparse.RawDescriptionHelpFormatter,
     epilog="""
Examples:
 python main.py path/to/resume.pdf
 python main.py --help
     """,
 )

 parser.add_argument("resume_path", help="Path to the PDF resume file to process")

 parser.add_argument("--verbose", action="store_true", help="Enable verbose output")

 args = parser.parse_args()

 if args.verbose:
     console.print("[yellow]Running in verbose mode[/yellow]")

 try:
     asyncio.run(process_resume(args.resume_path))
 except KeyboardInterrupt:
     console.print("\n[yellow]Process interrupted by user[/yellow]")
     sys.exit(1)


if __name__ == "__main__":
 main()
