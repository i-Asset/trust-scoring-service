node('iasset-jenkins-slave') {

    // -----------------------------------------------
    // --------------- Staging Branch ----------------
    // -----------------------------------------------
    if (env.BRANCH_NAME == 'staging') {

        stage('Clone and Update') {
            git(url: 'https://github.com/i-Asset/trust-scoring-service.git', branch: env.BRANCH_NAME)
        }

        stage('Build Dependencies') {
            sh 'rm -rf common-1'
            sh 'git clone https://github.com/i-Asset/common-1.git'
            dir('common-1') {
                sh 'git checkout ' + env.BRANCH_NAME
                sh 'mvn clean install'
            }
        }

        stage('Build Java') {
            sh 'mvn clean package -DskipTests'
        }

        stage('Build Docker') {
            sh 'mvn docker:build -P docker -DdockerImageTag=staging'
        }

        stage('Push Docker') {
            sh 'docker push iassetplatform/trust-service:staging'
        }

        stage('Deploy') {
            sh 'ssh staging "cd /srv/docker-setup/staging/ && ./run-staging.sh restart-single trust-service"'
        }
    }

    // -----------------------------------------------
    // ---------------- Master Branch ----------------
    // -----------------------------------------------
    if (env.BRANCH_NAME == 'master') {

        stage('Clone and Update') {
            git(url: 'https://github.com/i-Asset/trust-scoring-service.git', branch: env.BRANCH_NAME)
        }

        stage('Build Dependencies') {
            sh 'rm -rf common-1'
            sh 'git clone https://github.com/i-Asset/common-1.git'
            dir('common-1') {
                sh 'git checkout ' + env.BRANCH_NAME
                sh 'mvn clean install'
            }
        }

        stage('Build Java') {
            sh 'mvn clean package -DskipTests'
        }
    }

    // -----------------------------------------------
    // ---------------- Release Tags -----------------
    // -----------------------------------------------
    if( env.TAG_NAME ==~ /^\d+.\d+.\d+$/) {

        stage('Clone and Update') {
            git(url: 'https://github.com/nimble-platform/trust-scoring-service', branch: 'master')
        }

        stage('Build Dependencies') {
            sh 'rm -rf common'
            sh 'git clone https://github.com/nimble-platform/common'
            dir('common') {
                sh 'git checkout master'
                sh 'mvn clean install'
            }
        }

        stage('Set version') {
            sh 'mvn versions:set -DnewVersion=' + env.TAG_NAME
        }

        stage('Build Java') {
            sh 'mvn clean package -DskipTests'
        }

        stage('Build Docker') {
            sh 'mvn docker:build -P docker'
        }

        stage('Push Docker') {
            sh 'docker push nimbleplatform/trust-service:' + env.TAG_NAME
            sh 'docker push nimbleplatform/trust-service:latest'
        }

        stage('Deploy MVP') {
            sh 'ssh nimble "cd /data/deployment_setup/prod/ && sudo ./run-prod.sh restart-single trust-service"'
        }

        stage('Deploy FMP') {
            sh 'ssh fmp-prod "cd /srv/nimble-fmp/ && ./run-fmp-prod.sh restart-single trust-service"'
        }
    }
}
