node('docker') {
    try {
        stage('Checkout') {
            checkout scm
        }
        def version = sh( script: 'git describe --tags ', returnStdout: true).toString().trim()
        currentBuild.displayName = version
        stage('Build') {
            sh 'make'
        }
        stage('Archive') {
            archive 'app/build/outputs/apk/debug/*.apk'
        }
    } catch (exception) {
        currentBuild.result = 'FAILURE'
        step([$class: 'Mailer', recipients: '', notifyEveryUnstableBuild: true, sendToIndividuals: true])
        echo 'I failed with exception ' + exception.getMessage()
        throw exception
    }
}