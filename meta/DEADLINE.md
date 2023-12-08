# Deadline

Modify this file to satisfy a submission requirement related to the project
deadline. Please keep this file organized using Markdown. If you click on
this file in your GitHub repository website, then you will see that the
Markdown is transformed into nice-looking HTML.

## Part 1.1: App Description

> Please provide a friendly description of your app, including
> the primary functions available to users of the app. Be sure to
> describe exactly what APIs you are using and how they are connected
> in a meaningful way.

> **Also, include the GitHub `https` URL to your repository.**
This is the weather app that gives shows the current weather of a location based on the user input. The user have to input a city, then they can input a country and state if it is a US country for more specific results. The first API take the user input and returns the latitude and longitude, along with city name and country. The second API take the latitude and longitude to get the weather of that location.

https://github.com/AKLEEC/cs1302-api-app

## Part 1.2: APIs

> For each RESTful JSON API that your app uses (at least two are required), 
> include an example URL for a typical request made by your app. If you
> need to include additional notes (e.g., regarding API keys or rate 
> limits), then you can do that below the URL/URI. Placeholders for this
> information are provided below. If your app uses more than two RESTful
> JSON APIs, then include them with similar formatting.

### API 1

```
https://api.api-ninjas.com/v1/geocoding?city=tokyo
```

> include a header in HttpRequest: X-Api-Key=myApiKey

### API 2

```
https://api.openweathermap.org/data/2.5/weather?lat=50.234&lon=43.232&appid=myApiKey
```

## Part 2: New

> What is something new and/or exciting that you learned from working
> on this project?

I learned how to use resources to help me with the project instead of just my skills alone.

## Part 3: Retrospect

> If you could start the project over from scratch, what do
> you think might do differently and why?

At the beginning, I overestimated how APIs work because I thought that to implement 2 APIs, I would need to distinct function. But for example this weather API, it requires the latitude and longitude instead of typing in the city directly, so I need a helper API. So I should have fully grasp what a specific API need to do by doing more deeper research. Finding an API took me a week, but implementing it is very easy.