// import com.google.gson.Gson;
// import com.google.gson.reflect.TypeToken;
// import java.io.FileReader;
// import java.io.IOException;
// import java.lang.reflect.Type;
// import java.math.BigInteger;
// import java.util.ArrayList;
// import java.util.List;
// import java.util.Map;

// // A simple record to hold a point (x, y) using BigInteger.
// record Point(BigInteger x, BigInteger y) {}

// public class SecretFinder {

//     public static void main(String[] args) {
//         String filename = "input.json";
//         try {
//             // Step 1: Parse the input file to get the list of decoded points.
//             List<Point> points = parseInput(filename);

//             if (points == null || points.isEmpty()) {
//                 System.out.println("Could not parse points from the file.");
//                 return;
//             }

//             // Step 2: Calculate the secret 'c' using Lagrange Interpolation.
//             BigInteger secretC = calculateSecretC(points);

//             // Step 3: Print the final result.
//             System.out.println("The secret 'c' is:");
//             System.out.println(secretC);

//         } catch (IOException e) {
//             System.err.println("Error reading the file: " + filename);
//             e.printStackTrace();
//         }
//     }

//     /**
//      * Reads the JSON file, decodes the y-values, and returns a list of points.
//      *
//      * @param filename The name of the JSON input file.
//      * @return A list of (x, y) points.
//      * @throws IOException If the file cannot be read.
//      */
//     private static List<Point> parseInput(String filename) throws IOException {
//         Gson gson = new Gson();
//         FileReader reader = new FileReader(filename);

//         // Define the type for Gson to parse into: a Map of String keys to any Object value.
//         Type type = new TypeToken<Map<String, Object>>() {}.getType();
//         Map<String, Object> dataMap = gson.fromJson(reader, type);
//         reader.close();

//         // Extract 'k' from the nested "keys" object. Gson parses JSON numbers as Double by default.
//         Map<String, Object> keys = (Map<String, Object>) dataMap.get("keys");
//         int k = ((Double) keys.get("k")).intValue();

//         List<Point> points = new ArrayList<>();
//         System.out.println("Reading first " + k + " points from the input...");

//         // Loop from 1 to k to get the required number of points.
//         for (int i = 1; i <= k; i++) {
//             String key = String.valueOf(i);
//             Map<String, String> pointData = (Map<String, String>) dataMap.get(key);

//             // The 'x' value is the key itself.
//             BigInteger x = BigInteger.valueOf(i);

//             // Decode the 'y' value from the given base.
//             int base = Integer.parseInt(pointData.get("base"));
//             String value = pointData.get("value");
//             BigInteger y = new BigInteger(value, base);

//             // Add the fully decoded point to our list.
//             points.add(new Point(x, y));
//         }
//         return points;
//     }

//     /**
//      * Calculates the secret c = P(0) using Lagrange Interpolation.
//      *
//      * @param points The list of known points on the polynomial.
//      * @return The value of the polynomial at x=0, which is the constant 'c'.
//      */
//     private static BigInteger calculateSecretC(List<Point> points) {
//         BigInteger secretC = BigInteger.ZERO;

//         // Outer loop: Iterate through each point (xj, yj) to calculate its contribution.
//         for (Point pointJ : points) {
//             BigInteger yj = pointJ.y();
//             BigInteger xj = pointJ.x();

//             // This term represents yj * Lj(0).
//             // Lj(0) = product of [xi / (xi - xj)] for all i != j.
//             // We calculate the numerator and denominator of this product separately.
//             BigInteger termNumerator = yj;
//             BigInteger termDenominator = BigInteger.ONE;

//             // Inner loop: Build the numerator and denominator of Lj(0).
//             for (Point pointI : points) {
//                 // Skip if we are looking at the same point.
//                 if (pointI.equals(pointJ)) {
//                     continue;
//                 }
//                 BigInteger xi = pointI.x();

//                 // Numerator of Lj(0) is the product of all other x-values (xi).
//                 termNumerator = termNumerator.multiply(xi);

//                 // Denominator of Lj(0) is the product of (xi - xj).
//                 termDenominator = termDenominator.multiply(xi.subtract(xj));
//             }

//             // Calculate the full term for j. The division must be exact.
//             BigInteger term = termNumerator.divide(termDenominator);

//             // Add this term's contribution to the total sum for 'c'.
//             secretC = secretC.add(term);
//         }
//         return secretC;
//     }
// }



import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// A modern Java record to immutably hold a point (x, y). Requires JDK 16+.
record Point(BigInteger x, BigInteger y) {}

public class SecretFinder {

    public static void main(String[] args) {
        // The program now expects the filename to be passed as an argument.
        if (args.length == 0) {
            System.err.println("Usage: java SecretFinder <filename.json>");
            return;
        }
        String filename = args[0];

        try {
            // Step 1: Parse the input file to get the list of decoded points.
            List<Point> points = parseInput(filename);

            if (points == null || points.isEmpty()) {
                System.out.println("Could not parse points from the file.");
                return;
            }

            // Step 2: Calculate the secret 'c' using the correct Lagrange Interpolation method.
            BigInteger secretC = calculateSecretC(points);

            // Step 3: Print the final result.
            System.out.println("The secret 'c' for " + filename + " is:");
            System.out.println(secretC);

        } catch (IOException e) {
            System.err.println("Error reading the file: " + filename);
            e.printStackTrace();
        }
    }

    private static List<Point> parseInput(String filename) throws IOException {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(filename)) {
            Type type = new TypeToken<Map<String, Object>>() {}.getType();
            Map<String, Object> dataMap = gson.fromJson(reader, type);

            Map<String, Object> keys = (Map<String, Object>) dataMap.get("keys");
            int k = ((Double) keys.get("k")).intValue();

            List<Point> points = new ArrayList<>();
            System.out.println("Reading first " + k + " points from " + filename + "...");

            for (int i = 1; i <= k; i++) {
                String key = String.valueOf(i);
                Map<String, String> pointData = (Map<String, String>) dataMap.get(key);
                BigInteger x = BigInteger.valueOf(i);
                int base = Integer.parseInt(pointData.get("base"));
                String value = pointData.get("value");
                BigInteger y = new BigInteger(value, base);
                points.add(new Point(x, y));
            }
            return points;
        }
    }

    private static BigInteger calculateSecretC(List<Point> points) {
        BigInteger secretC = BigInteger.ZERO;
        for (Point pointJ : points) {
            BigInteger yj = pointJ.y();
            BigInteger xj = pointJ.x();

            BigInteger termNumerator = yj;
            BigInteger termDenominator = BigInteger.ONE;

            for (Point pointI : points) {
                if (pointI.equals(pointJ)) {
                    continue;
                }
                BigInteger xi = pointI.x();
                termNumerator = termNumerator.multiply(xi);
                termDenominator = termDenominator.multiply(xi.subtract(xj));
            }
            BigInteger term = termNumerator.divide(termDenominator);
            secretC = secretC.add(term);
        }
        return secretC;
    }
}