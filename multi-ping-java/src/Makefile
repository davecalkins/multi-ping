################################################################################

JAVAC         = javac
JAVAC_OPTS    = -g -deprecation
JAVAC_BSOPTS  = -source 1.3 -target 1.1
JAVA          = java
JAVA_OPTS     = -jar
JAR           = jar
JAR_OPTS      = cmf
MAINCLASSFILE = mainclass
RM            = rm
RM_OPTS       = -f
CP            = cp

INSTALL_DIR   = ../bin

################################################################################

SOURCES = \
	Log.java \
	TaskState.java \
	ProtoState.java \
	ProtoArgs.java \
	ProtoHandler.java \
	NetStreamConnection.java \
	PH_ICMP_Echo.java \
	PH_HTTP.java \
	PH_SMTP.java \
	PH_POP.java \
	ProtoMgr.java \
	HostProto.java \
	Host.java \
	BlockingQueue.java \
	PingListener.java \
	PingHost.java \
	PingTask.java \
	PingTaskResponse.java \
	PingMgr.java \
	HostEditorWnd.java \
	MainWnd.java \
	main.java \
	bootstrap.java

TARGET = multiping.jar

################################################################################

default: $(TARGET)

install:
	$(CP) $(TARGET) $(INSTALL_DIR)

clean:
	$(RM) $(RM_OPTS) *.class $(TARGET) $(INSTALL_DIR)/$(TARGET)

run: $(TARGET)
	$(JAVA) $(JAVA_OPTS) $(TARGET)

rebuild: clean $(TARGET)

$(TARGET): $(SOURCES:.java=.class)
	$(JAR) $(JAR_OPTS) $(MAINCLASSFILE) $(TARGET) *.class

bootstrap.class: bootstrap.java
	$(JAVAC) $(JAVAC_OPTS) $(JAVAC_BSOPTS) $<

.SUFFIXES: .java .class


.java.class:
	$(JAVAC) $(JAVAC_OPTS) $<



