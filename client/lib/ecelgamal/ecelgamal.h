//
// Created by Lukas Burkhalter on 01.02.18.
//

#ifndef ECELGAMAL_ECELGAMAL_H
#define ECELGAMAL_ECELGAMAL_H

#include <openssl/ec.h>
#include <openssl/bn.h>
#include <openssl/objects.h>
#include <inttypes.h>
#include "uthash.h"

#define DEFAULT_CURVE NID_X9_62_prime192v1
#define CURVE_256_SEC NID_X9_62_prime256v1

#define MAX_BITS 32

struct gamal_key {
    char is_public;
    EC_POINT *Y;
    BIGNUM *secret;
};

typedef struct gamal_key *gamal_key_ptr;
typedef struct gamal_key gamal_key_t[1];
typedef uint64_t dig_t;

size_t get_encoded_key_size(gamal_key_t key);
int encode_key(unsigned char* buff, int size, gamal_key_t key);
int decode_key(gamal_key_t key, unsigned char* buff, int size);

struct gamal_ciphertext {
    EC_POINT *C1;
    EC_POINT *C2;
};

typedef struct gamal_ciphertext *gamal_ciphertext_ptr;
typedef struct gamal_ciphertext gamal_ciphertext_t[1];

size_t get_encoded_ciphertext_size(gamal_ciphertext_t ciphertext);
int encode_ciphertext(unsigned char* buff, int size, gamal_ciphertext_t ciphertext);
int decode_ciphertext(gamal_ciphertext_t ciphertext, unsigned char* buff, int size);

typedef struct bsgs_hash_table_entry {
    unsigned char *key;
    uint32_t value;
    UT_hash_handle hh;
} bsgs_hash_table_entry_t;

struct bsgs_table_s {
    bsgs_hash_table_entry_t *table;
    EC_POINT *mG, *mG_inv;
    EC_GROUP *group;
    dig_t tablesize;
};
typedef struct bsgs_table_s *bsgs_table_ptr;
typedef struct bsgs_table_s bsgs_table_t[1];


int gamal_init(int curve_id);
int gamal_deinit();
EC_GROUP *gamal_get_current_group();
int gamal_get_point_compressed_size();
int gamal_init_bsgs_table(bsgs_table_t table, dig_t size);
int gamal_free_bsgs_table(bsgs_table_t table);

int gamal_key_clear(gamal_key_t keys);
int gamal_cipher_clear(gamal_ciphertext_t cipher);
int gamal_cipher_new(gamal_ciphertext_t cipher);

int gamal_generate_keys(gamal_key_t keys);
int gamal_encrypt(gamal_ciphertext_t ciphertext, gamal_key_t key, dig_t plaintext);
int gamal_decrypt(dig_t *res, gamal_key_t key, gamal_ciphertext_t ciphertext, bsgs_table_t table);
int gamal_add(gamal_ciphertext_t res, gamal_ciphertext_t ciphertext1, gamal_ciphertext_t ciphertext2);

#endif //ECELGAMAL_ECELGAMAL_H
