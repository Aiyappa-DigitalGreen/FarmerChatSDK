import SwiftUI

// MARK: - FollowUpQuestionsBar

/// Vertical list of related questions — each row has question text + green Ask button.
struct FollowUpQuestionsBar: View {

    let questions: [String]
    let questionIds: [String]?
    let onTap: (String, String?) -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Related questions")
                .font(.system(size: 15, weight: .bold))
                .foregroundColor(.primary)

            ForEach(Array(questions.enumerated()), id: \.offset) { index, question in
                let questionId: String? = questionIds?.indices.contains(index) == true
                    ? questionIds?[index]
                    : nil

                RelatedQuestionRow(question: question) {
                    onTap(question, questionId)
                }
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
    }
}

// MARK: - RelatedQuestionRow

private struct RelatedQuestionRow: View {

    let question: String
    let onTap: () -> Void

    var body: some View {
        HStack(spacing: 10) {
            Text(question)
                .font(.system(size: 14))
                .foregroundColor(.primary)
                .multilineTextAlignment(.leading)
                .fixedSize(horizontal: false, vertical: true)
                .frame(maxWidth: .infinity, alignment: .leading)

            Button(action: onTap) {
                Text("Ask")
                    .font(.system(size: 13, weight: .semibold))
                    .foregroundColor(.white)
                    .padding(.horizontal, 14)
                    .padding(.vertical, 8)
                    .background(SDKColors.primary)
                    .cornerRadius(8)
            }
            .fixedSize()
        }
        .padding(.leading, 14)
        .padding(.trailing, 10)
        .padding(.vertical, 12)
        .background(Color(.systemGray6))
        .cornerRadius(12)
    }
}
