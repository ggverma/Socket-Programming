import java.util.Scanner;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Stack;

public class Server{
  private static double input;

  private static PrintWriter out;

  public static void main(String[] args) throws Exception{
    if(args.length != 1){
      System.out.println("Please specify port number.");
      System.exit(0);
    }
    int port = Integer.parseInt(args[0]);
    System.out.println("Server is Up.");
    ServerSocket listener = null;
    try{
      // Listen to this port
      listener = new ServerSocket(port);
      while(true){
        Socket socket = listener.accept();
        try{
          Scanner sc = new Scanner(socket.getInputStream());
          out = new PrintWriter(socket.getOutputStream(), true);
          // Get the input
          input = Integer.parseInt(sc.nextLine().split("=")[1].trim());
          // Send the result to client
          out.println(evaluateExpression(sc.nextLine()));
        }catch(ArithmeticException e){
          out.println("A divide by zero occured while evaluating.");
        }finally{
          socket.close();
        }
      }
    }catch (IOException e) {
      e.printStackTrace();
    }finally{
      if(listener != null){
        listener.close();
      }
    }
  }

  // Evaluates the expression sent by Client.
  // Works similar to polish notation.
  // NOTE: This can evaluate only single variable expressions
  private static double evaluateExpression(String expression){
    // See comment on line 59 to understand use of stack.
    Stack<StackEntry> stack = new Stack<>();
    // I use wasLastInput to use variables with length of name > 1.
    boolean wasLastInput = false;
    // Used for manipulation in stack.
    double lastResult;
    StackEntry lastEntry, lastResultEntry;
    for(char c : expression.toCharArray()){
      // stack holds the results evaluated until the character c is read.
      // If blankspace, continue
      if(c == ' ') continue;
      // These are the keywords in the expression. If these are encountered, add
      // them to the stack.
      if((c >= 'A' && c <= 'E') || (c == '(') || (c == '+' || c == '-' || c == '*' || c == '/')){
        // Clear wasLastInput because it will not be true next time.
        wasLastInput = false;
        stack.push(new StackEntry(c));
      }else if(c == ')'){
      // Evaluate until '(' is encountered in the stack.
        // Clear wasLastInput because it will not be true next time.
        wasLastInput = false;
        while(true){
          // Evaluate the stack until expressions of form num1 + num2 / num3 - ... are present.
          // At each iteration a result of the form x op y is evaluated and pushed back.
          // Here, x is given by lastResultEntry
          // And y is given by lastEntry;
          // op is Arithmetic function.
          lastResultEntry = stack.pop();
          lastEntry = stack.pop();
          if(lastEntry.isOp && lastEntry.op == '('){
            // break now while pushing the lastResultEntry.
            stack.push(lastResultEntry);
            break;
          }
          lastResult = lastResultEntry.result;
          double anotherResult = stack.pop().result;
          switch (lastEntry.op) {
            case '+':
              stack.push(new StackEntry(lastResult + anotherResult));
              break;
            case '-':
              stack.push(new StackEntry(anotherResult - lastResult));
              break;
            case '*':
              stack.push(new StackEntry(lastResult * anotherResult));
              break;
            case '/':
              stack.push(new StackEntry(anotherResult / lastResult));
              break;
          }
        }
        // Now, a function call may be present before the '('. If yes, evaluate
        // the function. Otherwise, continue evaluating the expression.
        lastEntry = stack.pop();
        lastResult = lastEntry.result;
        if(stack.isEmpty()){
          stack.push(lastEntry);
          continue;
        }
        lastEntry = stack.pop();
        switch(lastEntry.op){
          case 'A':
            stack.push(new StackEntry(lastResult * lastResult));
            break;
          case 'B':
            stack.push(new StackEntry(lastResult * 10));
            break;
          case 'C':
            stack.push(new StackEntry(lastResult + 20));
            break;
          case 'D':
            stack.push(new StackEntry(Math.log10(lastResult)));
            break;
          case 'E':
            stack.push(new StackEntry(Math.sqrt(lastResult)));
            break;
        }
      }else if(!wasLastInput){
        // Scanned the input variable
        wasLastInput = true;
        stack.push(new StackEntry(input));
      }
    }
    // After reading the expression, the stack may still contain multiple entries.
    // For eg. 4 - 2 + 3
    // Evaluate and return the result.
    while(true){
      lastResultEntry = stack.pop();
      if(stack.isEmpty()){
        // Completed evaluation.
        return lastResultEntry.result;
      }
      lastEntry = stack.pop();
      lastResult = lastResultEntry.result;
      double anotherResult = stack.pop().result;
      switch (lastEntry.op) {
        case '+':
          stack.push(new StackEntry(lastResult + anotherResult));
          break;
        case '-':
          stack.push(new StackEntry(anotherResult - lastResult));
          break;
        case '*':
          stack.push(new StackEntry(lastResult * anotherResult));
          break;
        case '/':
          stack.push(new StackEntry(anotherResult / lastResult));
          break;
      }
    }
  }

  private static class StackEntry{
    // op = operation/operator
    // Tell whether this entry represents function call or Arithmetic operation.
    char op;
    // If this is not an operation, it is an intermediate result.
    double result;
    boolean isOp;

    public StackEntry(char op){
      this.op = op;
      isOp = true;
    }

    public StackEntry(double result){
      this.result = result;
      op = '%';
      isOp = false;
    }
  }
}
