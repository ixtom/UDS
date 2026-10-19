# The Ultimate Dojo Showdown (UDS)
## Abstract
UDS is a web app which facilitates 1v1 animation competitions called “duels” between online animators. This competition system was called the “RHG system”, which was used in a historical animation website from the early 2000s. Users can sign up to participate or vote in the animation duels. Animators will face off against an opponent chosen at random, where each will create animations of their “duelist” character fighting the other.  After a predetermined amount of time, submissions will end and the voting period will start. Users will vote on who’s animation is better, in multiple factors such as motion, choreography, story, and art direction. At the end of each round, those with the fewest votes will get eliminated before the next round starts. Users will be able to customize their profiles, view past competitions and quickly find their own animations.

## The Problem
Communities of web animators used to have a platform where they competed against other animators using the RHG system, however these platforms have since shut down. This RHG system is over 20 years old, having created many professional animators. Losing the RHG system would be losing important animation history.

## Other (Current) Solutions
A Discord server was created to replace the now defunct websites, however this solution degraded the rating system along with removing all gamified aspects of the competitions. Organized events hosted on Discord and Youtube have seen success, but required more manual effort from the organizers as opposed to previous RHG websites. 

## How Will This Be Better
Since the old platform shut down, the community has relied on Discord, however Discord lacks features from the old platform such as displaying character information, eliminating losers, and a well-designed voting system since it is primarily meant for messaging and group communication. Furthermore, looking for animations or duels is difficult since they can become lost in chat, and being restricted to Discord limits community growth since new users need a Discord account and an invite to the server rather than simply going on a website. Additionally, Discord is a very casual community which leads to a large amount of low quality or unfinished work being submitted making quality control more difficult. A dedicated platform for this niche community would restore these features and make it easier for people to discover it.

## Target audience
This web app targets aspiring and professional online animators within stickfigure communities who wish to improve their animation skills and highlight their original characters in a competitive setting.

## Scope
This project has a handful of features and one larger one with a few subproblems, which should be enough work for five developers.

## Epics
This project can be broken down into five main epics: Account Authentication, User Profiles, Competition Management, Duels & Voting, and Platform & Discovery.

Account authentication includes creation, deletion, changing or resetting passwords, and persistent logins. To prevent users creating multiple accounts, we will require users to link discord and verify their emails and use Cloudflare's turnstile to prevent bots.

User profiles cover customization such as linking social platforms, an about section, and an area to showcase work from competitions. Administrator accounts will be able to manage users that may be violating rules by unlisting, deleting, or banning users. User profiles also cover duelist customization, allowing users to upload text information and images to their duelist profile pages.

Competition management contains the systems needed for most competitions in general. Creation and configuration can be done by administrators. Users will be able to join and leave. Other features include assigning opponents, displaying current status, eliminations, and rankings. 

Duels and voting is the core of the competition. This covers adding and displaying animations and duelists’ background, and the voting process.

Platform and discovery will round out the website, tying everything together by allowing users to find and view competitions/duels, global rankings through the home page and other discovery pages.

## APIs

Discord for login (Oauth 2\) [docs](https://docs.discord.com/developers/topics/oauth2)  
Discord for user date (profile picture, username/user ID) [docs](https://docs.discord.com/developers/reference)  
Potentially:  
Email via gmail [docs](https://developers.google.com/workspace/gmail/api/reference/rest/v1/users.messages/send)  
Turnstile (captcha) [docs](https://www.cloudflare.com/products/turnstile/)  

## Project presentation
[Google Slides](https://docs.google.com/presentation/d/1CcUp0CMQ6qd7-9cSG714000Sr8k5Szlc58Ow19JUcR4/edit?usp=sharing)