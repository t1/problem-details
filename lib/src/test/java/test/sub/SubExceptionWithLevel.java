package test.sub;

import org.eclipse.microprofile.problemdetails.Logging;

import static org.eclipse.microprofile.problemdetails.LogLevel.INFO;

@Logging(at = INFO)
public class SubExceptionWithLevel extends Exception {}
