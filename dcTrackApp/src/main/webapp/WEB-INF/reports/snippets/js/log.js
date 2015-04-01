//INTIALIZE THE LOGGER WITH A FILE BASED LOGGER
importPackage(Packages.org.apache.log4j);

function createLogFn(fnname) {
    //need to init logger lazy, otherwise birts raises an error if previewing data source -> reason: unknown!
    fn = "__fn = function " + fnname + "(str) {"
    fn+= "if (logger == null) logger = Logger.getLogger('ReportLogger');";
    fn+= "prefix = this.getName ? this.getName() + ': ' : '';";
    fn+= "logger." + fnname + "(prefix + str);}";
    eval(fn);
    reportContext.setPersistentGlobalVariable(fnname, __fn);
}

var logger = null;

createLogFn("debug");
createLogFn("info");
createLogFn("warn");
createLogFn("error");
createLogFn("fatal");
