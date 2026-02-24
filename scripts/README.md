
# Football Data Scripts

This directory contains scripts for fetching football data (schedule, teams, etc.).

## Setup

1. Install dependencies:
   ```bash
   pip install -r requirements.txt
   ```

## Scripts

### `fetch_schedule.py`

Fetches match schedule and updates the database.
It also includes logic to **update team logos** if they are missing or using local paths.

To run:
```bash
python fetch_schedule.py
```

## Customization

You can modify `fetch_schedule_from_source` and `fetch_team_logo` in `fetch_schedule.py` to integrate with your preferred data source (API or Web Scraper).
