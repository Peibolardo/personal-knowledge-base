package backend.utils;

/**
 * Utility methods for preparing embedding vectors for PostgreSQL vector operations.
 */
public class VectorUtils {

    private VectorUtils(){}

    /**
     * Converts a primitive embedding array into PostgreSQL's vector literal format.
     *
     * <p>The returned value is a comma-separated list of numbers enclosed in square brackets,
     * for example {@code [0.1,0.2,0.3]}. It can be bound as text and cast to PostgreSQL's
     * {@code vector} type in a query. The values are appended directly because Java's
     * {@code Arrays.stream} does not support primitive {@code float[]} arrays.</p>
     *
     * @param embedding primitive float values returned by the embedding service
     * @return a PostgreSQL-compatible vector literal
     * @throws IllegalArgumentException if the array is null, empty, or contains a non-finite value
     */
    public static String toVectorString(float[] embedding) {

        if (embedding == null || embedding.length == 0) {
            throw new IllegalArgumentException("Embedding cannot be null or empty");
        }

        StringBuilder vector = new StringBuilder("[");

        for (int index = 0; index < embedding.length; index++) {
            float value = embedding[index];
            if (!Float.isFinite(value)) {
                throw new IllegalArgumentException("Embedding values must be finite");
            }

            if (index > 0) {
                vector.append(',');
            }
            vector.append(value);
        }

        return vector.append(']').toString();
    }

}
