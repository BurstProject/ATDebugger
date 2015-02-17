package attools

sealed class ATToolsException extends Exception
case class DuplicateLabelException(label: String) extends ATToolsException
case class UnknownLineException(line: String) extends ATToolsException
case class LabelNotFoundException(label: String) extends ATToolsException
case class VariableNotFoundException(variable: String) extends ATToolsException
case class FunctionNotFoundException(fun: String) extends ATToolsException
