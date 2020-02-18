package test.sub;

import org.eclipse.microprofile.problemdetails.Logging;

@Logging(to = "sub-cat")
public class SubExceptionWithCategory extends Exception {}
