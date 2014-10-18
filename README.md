This is a backend for teamstats tool. It's goal is to provide basic monitoring on team performance in terms of working with version control.

Main questions answered by the tool:
- what is team commit history
- who are the owners of different parts of the code
- how much does the content from different committers differ (code vs test, addition vs deletions, etc)
  
API description https://docs.google.com/document/d/1PQZDj6yOI9rIKIa7rFiQhb0jXHPBURhvf-ly8nRCyPU/edit

##Calls summary

<b>GET /overview/flat</b>

returns: 

    [
        {
            id: %id%,
            name: %name%,
            first_commit_time: %time_string%,
            last_commit_time: %time_string%
        }
    ]

example for <i>"http://svn.apache.org/repos/asf/spamassassin/trunk/"</i>:

    [{"id":"dos","name":"dos","startTime":1110512222282,"endTime":1308956981731},
    {"id":"parker","name":"parker","startTime":1088445863814,"endTime":1219516966976},
    {"id":"jquinn","name":"jquinn","startTime":1395356651477,"endTime":1410526536567},
    {"id":"fanf","name":"fanf","startTime":1164389243782,"endTime":1270644535082},
    {"id":"hstern","name":"hstern","startTime":1089245158891,"endTime":1389804208697},
    {"id":"dlemke","name":"dlemke","startTime":1338798215370,"endTime":1338798215370},
    {"id":"felicity","name":"felicity","startTime":1088393583147,"endTime":1228106340436},
    {"id":"maddoc","name":"maddoc","startTime":1132375146060,"endTime":1295285046828},
    {"id":"sidney","name":"sidney","startTime":1088390315622,"endTime":1370176767198},
    {"id":"jhardin","name":"jhardin","startTime":1245604951615,"endTime":1413232562413},
    {"id":"hege","name":"hege","startTime":1248711029710,"endTime":1411216078083},
    {"id":"duncf","name":"duncf","startTime":1094485591981,"endTime":1388618208687},
    {"id":"spamassassin_role","name":"spamassassin_role","startTime":1267241258248,"endTime":1413341165860},
    {"id":"smf","name":"smf","startTime":1296667638495,"endTime":1407172430121},
    {"id":"striker","name":"striker","startTime":1088341191957,"endTime":1088341191957},
    {"id":"quinlan","name":"quinlan","startTime":1088394970772,"endTime":1154550535553},
    {"id":"jm","name":"jm","startTime":1088394729424,"endTime":1307822664109},
    {"id":"kb","name":"kb","startTime":1208178210859,"endTime":1402355863053},
    {"id":"kmcgrail","name":"kmcgrail","startTime":1141223475454,"endTime":1412779598076},
    {"id":"wtogami","name":"wtogami","startTime":1254691056996,"endTime":1323747103889},
    {"id":"jgmyers","name":"jgmyers","startTime":1129150765522,"endTime":1265234601260},
    {"id":"mss","name":"mss","startTime":1088446448013,"endTime":1186702033016},
    {"id":"mmartinec","name":"mmartinec","startTime":1186757931215,"endTime":1412080258311},
    {"id":"khopesh","name":"khopesh","startTime":1258692998052,"endTime":1409852963975},
    {"id":"axb","name":"axb","startTime":1247253936122,"endTime":1413300388984}]
    
    
##Run instructions
    
In IntelliJIDEA run src/main/scala/teamstats/ServerLogic.scala:Server. It will start an http server and open a browser to a index page. As we do not have that yet, you'll need manually add a path of a desired REST endpoint to the browser URL.

For the application to function a running mongodb is required. http://www.mongodb.org/downloads . Use <i>mongod</i> command to run it.