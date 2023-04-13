<p align="center">
    <img width="500px" src="https://github.com/aljoshakoecher/BPMN-Skill-Executor/blob/documentation/images/documentation/images/BPMN-SkillExecutor_Logo.png?raw=true">
</p>
<h1 align="center">Orchestrate Skill Processes using BPMN</h1>
<br>

A Plugin for [Camunda BPMN Platform](https://docs.camunda.org/manual/7.16/). Tested with the distribution based on Apache Tomcat.
This is a lightweigh plugin that is capable of executing skills from a BPMN process execution. BPMN processes created with [SkillMEx](https://github.com/aljoshakoecher/SkillMEx) reference this plugin for delegation. When a process with skills is executed, the Camunda BPMN engine stops at a skill tasks and delegates it to this plugin.

## Usage:
:grey_exclamation: This executor is a plugin that is built for the Camunda process engine. So first, download Camunda at https://camunda.com/download/platform-7/. This plugin was tested with the Tomcat edition and the following infos are for this edition. But it should also work with the "run" edition.
The most simple way to use this BPMN skill executor is to take a jar from the releases (make sure to use a version compatible with SkillMEx) and copy it into the lib folder inside `<your-camunda directory>/server/apache-tomcat/lib`.

:grey_exclamation: Make sure to copy it into the correct folder. There is also a `lib` directory inside `<your-camunda directory>`, this is the wrong one. The BPMN executor has to be copied inside the `lib` folder within `the apache-tomcat` folder.

As soon as you copied the executor into the folder, you're all set. You just have to start Camunda and the engine will use this plugin for every skill to invoke.

Of course, you can also extend this code and build it yourself using Maven.
