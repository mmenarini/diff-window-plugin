This project is an IntelliJ IDEA plugin that shows a window which can display diffs.

<img id="build-status" src="https://api.travis-ci.org/sandervalstar/diff-window-plugin.png?branch=master" href="https://travis-ci.org/sandervalstar/diff-window-plugin">
<br>

How to develop:
* Install IntelliJ IDEA
* Install the Lombok Plugin for IntelliJ under "Preferences > Plugins"
* Then go to "Preferences" search for "annotation processors" and check "Enable annotation processing"
* Run the project using "./gradlew runIde" or the "intellij/runIde" task in the gradle tool window.



<script>
    var date = new Date();
    var time = Math.trunc(date.getTime()/60000);
    var link = document.getElementById("build-status");
    link.setAttribute("src", link.getAttribute("src")+"&date="+time);
</script>
